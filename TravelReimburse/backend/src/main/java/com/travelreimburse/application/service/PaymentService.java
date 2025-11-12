package com.travelreimburse.application.service;

import com.travelreimburse.domain.event.payment.PaymentFailedEvent;
import com.travelreimburse.domain.event.payment.PaymentInitiatedEvent;
import com.travelreimburse.domain.event.payment.PaymentSuccessEvent;
import com.travelreimburse.domain.exception.CannotSubmitPaymentException;
import com.travelreimburse.domain.model.PaymentRequest;
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

/**
 * Application Service für Payment-Management.
 * Orchestriert Use Cases - KEINE Business-Logik hier!
 * Business-Logik läuft in Entity und Domain Service.
 *
 * Flow: Payment erstellen → EasyPay senden (PROCESSING) → Async completion → TravelRequest PAID
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
     * Use Case: Erstelle Payment und sende zu EasyPay
     *
     * Flow:
     * 1. PaymentRequest erstellen (Status: PENDING)
     * 2. An EasyPay senden → TransactionId erhalten
     * 3. Status → PROCESSING
     * 4. Async completion erfolgt später (siehe completePayment)
     *
     * @return PaymentRequestDTO mit Status PROCESSING
     */
    @Transactional
    public PaymentRequestDTO createAndSubmitPayment(Long travelRequestId) {
        logger.info("Initiating payment for travel request: {}", travelRequestId);

        TravelRequest travelRequest = travelRequestRepository.findById(travelRequestId)
            .orElseThrow(() -> new IllegalArgumentException("TravelRequest nicht gefunden: " + travelRequestId));

        // 1. PaymentRequest erstellen
        PaymentRequest paymentRequest = paymentInitiationService.createPaymentRequest(travelRequest);
        paymentRequest = paymentRepository.save(paymentRequest);

        try {
            // 2. An EasyPay senden
            EasyPayAdapter.EasyPayResponse easyPayResponse = easyPayAdapter.submitPayment(paymentRequest);
            String transactionId = easyPayResponse.transactionId();

            // 3. Status → PROCESSING (nicht sofort SUCCESS!)
            paymentRequest.markAsProcessing(transactionId);
            paymentRequest = paymentRepository.save(paymentRequest);

            logger.info("Payment {} submitted with TransactionID: {} - Status: PROCESSING",
                    paymentRequest.getId(), transactionId);

            // 4. 🎯 Publish Domain Event für asynchrone Verarbeitung
            eventPublisher.publishEvent(new PaymentInitiatedEvent(
                paymentRequest.getId(),
                travelRequestId,
                transactionId,
                LocalDateTime.now()
            ));

            logger.info("📢 Published PaymentInitiatedEvent for payment: {}", paymentRequest.getId());

        } catch (EasyPayException e) {
            logger.error("Fehler beim Absenden zu EasyPay: {}", e.getMessage());
            paymentRequest.markAsFailed(e.getMessage());
            paymentRepository.save(paymentRequest);
            throw new CannotSubmitPaymentException(paymentRequest.getId(), e.getMessage());
        }

        return mapper.toDTO(paymentRequest);
    }

    /**
     * Use Case: Schließe Payment ab (wird von Event Listener aufgerufen)
     *
     * Flow:
     * 1. Payment Status PROCESSING → SUCCESS/FAILED
     * 2. Domain Event publishen
     * 3. PaymentEventHandler reagiert auf Event und updated TravelRequest
     *
     * ⚠️ PaymentService kümmert sich NUR um PaymentRequest - NICHT um TravelRequest!
     * ⚠️ KEINE Archivierung hier - das ist eine separate Verantwortung!
     */
    @Transactional
    public void completePayment(Long paymentId, boolean success, String message) {
        logger.info("Completing payment: {} - Success: {}", paymentId, success);

        PaymentRequest paymentRequest = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment nicht gefunden: " + paymentId));

        if (success) {
            paymentRequest.markAsSuccess();
            paymentRepository.save(paymentRequest);

            logger.info("✅ Payment {} marked as SUCCESS", paymentId);

            // 🎯 Publish PaymentSuccessEvent
            // PaymentEventHandler wird TravelRequest Status auf PAID setzen
            eventPublisher.publishEvent(new PaymentSuccessEvent(
                paymentRequest.getId(),
                paymentRequest.getTravelRequest().getId(),
                paymentRequest.getEasyPayTransactionId(),
                LocalDateTime.now()
            ));

            logger.info("📢 Published PaymentSuccessEvent for payment: {}", paymentId);

        } else {
            paymentRequest.markAsFailed(message != null ? message : "Payment failed");
            paymentRepository.save(paymentRequest);

            logger.error("❌ Payment {} marked as FAILED: {}", paymentId, message);

            // 🎯 Publish PaymentFailedEvent
            eventPublisher.publishEvent(new PaymentFailedEvent(
                paymentRequest.getId(),
                paymentRequest.getTravelRequest().getId(),
                message != null ? message : "Payment failed",
                LocalDateTime.now()
            ));

            logger.info("📢 Published PaymentFailedEvent for payment: {}", paymentId);
        }
    }

    /**
     * Use Case: Hole Payment-Status
     */
    public PaymentRequestDTO getPaymentStatus(Long paymentId) {
        PaymentRequest payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment nicht gefunden: " + paymentId));

        return mapper.toDTO(payment);
    }


    /**
     * Use Case: Finde Payment nach Referenz
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

