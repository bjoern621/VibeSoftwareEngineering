package com.travelreimburse.application.service;

import com.travelreimburse.domain.event.PaymentFailedEvent;
import com.travelreimburse.domain.event.PaymentSuccessEvent;
import com.travelreimburse.domain.exception.CannotSubmitPaymentException;
import com.travelreimburse.domain.model.PaymentRequest;
import com.travelreimburse.domain.model.PaymentStatus;
import com.travelreimburse.domain.model.TravelRequest;
import com.travelreimburse.domain.repository.PaymentRequestRepository;
import com.travelreimburse.domain.repository.TravelRequestRepository;
import com.travelreimburse.domain.service.PaymentInitiationService;
import com.travelreimburse.infrastructure.external.easypay.EasyPayAdapter;
import com.travelreimburse.infrastructure.external.easypay.EasyPayException;
import com.travelreimburse.presentation.dto.PaymentRequestDTO;
import com.travelreimburse.presentation.dto.PaymentRequestMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Application Service für Payment-Management.
 * Orchestriert Use Cases - KEINE Business-Logik hier!
 * Business-Logik läuft in Entity und Domain Service.
 */
@Service
@Transactional(readOnly = true)
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRequestRepository paymentRepository;
    private final TravelRequestRepository travelRequestRepository;
    private final PaymentInitiationService paymentInitiationService;
    private final EasyPayAdapter easyPayAdapter;
    private final PaymentRequestMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    public PaymentService(
            PaymentRequestRepository paymentRepository,
            TravelRequestRepository travelRequestRepository,
            PaymentInitiationService paymentInitiationService,
            EasyPayAdapter easyPayAdapter,
            PaymentRequestMapper mapper,
            ApplicationEventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.travelRequestRepository = travelRequestRepository;
        this.paymentInitiationService = paymentInitiationService;
        this.easyPayAdapter = easyPayAdapter;
        this.mapper = mapper;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Use Case 1: Erstelle Payment und sende zu EasyPay
     */
    @Transactional
    public PaymentRequestDTO createAndSubmitPayment(Long travelRequestId) {
        logger.info("Creating payment for travel request: {}", travelRequestId);

        TravelRequest travelRequest = travelRequestRepository.findById(travelRequestId)
            .orElseThrow(() -> new IllegalArgumentException("TravelRequest nicht gefunden: " + travelRequestId));

        PaymentRequest paymentRequest = paymentInitiationService.createPaymentRequest(travelRequest);
        paymentRequest = paymentRepository.save(paymentRequest);

        try {
            EasyPayAdapter.EasyPayResponse easyPayResponse = easyPayAdapter.submitPayment(paymentRequest);

            paymentRequest.submitToEasyPay();
            paymentRequest.markAsProcessing();
            paymentRequest = paymentRepository.save(paymentRequest);

            logger.info("Payment {} zu EasyPay übermittelt", paymentRequest.getId());

        } catch (EasyPayException e) {
            logger.error("Fehler beim Absenden zu EasyPay: {}", e.getMessage());
            paymentRequest.markAsFailed(e.getMessage());
            paymentRepository.save(paymentRequest);
            throw new CannotSubmitPaymentException(paymentRequest.getId(), e.getMessage());
        }

        return mapper.toDTO(paymentRequest);
    }

    /**
     * Use Case 2: Verarbeite EasyPay Callback
     */
    @Transactional
    public void handlePaymentCallback(String easyPayTransactionId, String status, String reason) {
        logger.info("EasyPay Callback empfangen - TransactionId: {}, Status: {}", easyPayTransactionId, status);

        PaymentRequest payment = paymentRepository.findByEasyPayTransactionId(easyPayTransactionId)
            .orElseThrow(() -> new IllegalArgumentException("Payment nicht gefunden: " + easyPayTransactionId));

        if ("SUCCESS".equalsIgnoreCase(status)) {
            payment.markAsSuccess(easyPayTransactionId);
            paymentRepository.save(payment);

            eventPublisher.publishEvent(new PaymentSuccessEvent(
                payment.getId(),
                payment.getTravelRequest().getId(),
                easyPayTransactionId,
                LocalDateTime.now()
            ));

            logger.info("Payment {} erfolgreich markiert", payment.getId());

        } else if ("FAILED".equalsIgnoreCase(status)) {
            payment.markAsFailed(reason != null ? reason : "Unbekannter Fehler");
            paymentRepository.save(payment);

            eventPublisher.publishEvent(new PaymentFailedEvent(
                payment.getId(),
                payment.getTravelRequest().getId(),
                reason != null ? reason : "Unbekannter Fehler",
                LocalDateTime.now()
            ));

            logger.error("Payment {} fehlgeschlagen: {}", payment.getId(), reason);
        }
    }

    /**
     * Use Case 3: Hole Payment-Status
     */
    public PaymentRequestDTO getPaymentStatus(Long paymentId) {
        PaymentRequest payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment nicht gefunden: " + paymentId));

        return mapper.toDTO(payment);
    }

    /**
     * Use Case 4: Finde alle ausstehenden Zahlungen
     */
    public List<PaymentRequestDTO> findPendingPayments() {
        return paymentRepository.findAllWithStatus(PaymentStatus.PENDING)
            .stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Use Case 5: Finde alle fehlgeschlagenen Zahlungen
     */
    public List<PaymentRequestDTO> findFailedPayments() {
        return paymentRepository.findAllFailedPayments()
            .stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Use Case 6: Wiederholen fehlgeschlagener Zahlung
     */
    @Transactional
    public PaymentRequestDTO retryFailedPayment(Long paymentId) {
        logger.info("Retrying failed payment: {}", paymentId);

        PaymentRequest payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment nicht gefunden: " + paymentId));

        if (!payment.canBeRetried()) {
            throw new CannotSubmitPaymentException(paymentId,
                "Nur Payments im Status FAILED können wiederholt werden (aktuell: " + payment.getStatus() + ")");
        }

        return createAndSubmitPayment(payment.getTravelRequest().getId());
    }

    /**
     * Use Case 7: Finde Payment nach Referenz
     */
    public PaymentRequestDTO findByPaymentReference(String reference) {
        var payment = paymentRepository.findByPaymentReference(
            com.travelreimburse.domain.model.PaymentReference.of(reference)
        );

        return payment
            .map(mapper::toDTO)
            .orElseThrow(() -> new IllegalArgumentException("Payment mit Referenz nicht gefunden: " + reference));
    }
}

