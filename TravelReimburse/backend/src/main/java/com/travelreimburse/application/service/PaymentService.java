package com.travelreimburse.application.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application Service für Payment-Management.
 * Orchestriert Use Cases - KEINE Business-Logik hier!
 * Business-Logik läuft in Entity und Domain Service.
 *
 * Flow: Payment erstellen → EasyPay (gemockt) → Sofort SUCCESS → TravelRequest PAID → Archivierung
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
    private final ArchivingService archivingService;

    public PaymentService(
            PaymentRequestRepository paymentRepository,
            TravelRequestRepository travelRequestRepository,
            PaymentInitiationService paymentInitiationService,
            EasyPayAdapter easyPayAdapter,
            PaymentRequestMapper mapper,
            ArchivingService archivingService) {
        this.paymentRepository = paymentRepository;
        this.travelRequestRepository = travelRequestRepository;
        this.paymentInitiationService = paymentInitiationService;
        this.easyPayAdapter = easyPayAdapter;
        this.mapper = mapper;
        this.archivingService = archivingService;
    }

    /**
     * Use Case: Erstelle Payment, sende zu EasyPay (gemockt), markiere als SUCCESS
     *
     * 1. PaymentRequest erstellen
     * 2. EasyAdapter.submitPayment() - gibt sofort transactionId zurück (gemockt)
     * 3. Payment Status → SUCCESS
     * 4. TravelRequest Status → PAID
     * 5. Automatisch archivieren
     */
    @Transactional
    public PaymentRequestDTO createAndSubmitPayment(Long travelRequestId) {
        logger.info("Creating payment for travel request: {}", travelRequestId);

        TravelRequest travelRequest = travelRequestRepository.findById(travelRequestId)
            .orElseThrow(() -> new IllegalArgumentException("TravelRequest nicht gefunden: " + travelRequestId));

        PaymentRequest paymentRequest = paymentInitiationService.createPaymentRequest(travelRequest);
        paymentRequest = paymentRepository.save(paymentRequest);

        try {
            // EasyPay Adapter gibt sofort transactionId zurück (MOCK)
            EasyPayAdapter.EasyPayResponse easyPayResponse = easyPayAdapter.submitPayment(paymentRequest);
            String transactionId = easyPayResponse.transactionId();

            // ⭐ Sofort SUCCESS - KEINE PROCESSING BULLSHIT
            paymentRequest.markAsSuccess(transactionId);
            paymentRequest = paymentRepository.save(paymentRequest);

            logger.info("Payment {} erfolgreich mit TransactionID: {}", paymentRequest.getId(), transactionId);

            // ⭐ TravelRequest auf PAID setzen
            travelRequest.pay();
            travelRequestRepository.save(travelRequest);

            logger.info("TravelRequest {} Status → PAID", travelRequestId);

            // ⭐ Automatisch archivieren
            archivingService.archiveAfterPaymentSuccess(travelRequestId);

            logger.info("TravelRequest {} Status → ARCHIVED", travelRequestId);

        } catch (EasyPayException e) {
            logger.error("Fehler beim Absenden zu EasyPay: {}", e.getMessage());
            paymentRequest.markAsFailed(e.getMessage());
            paymentRepository.save(paymentRequest);
            throw new CannotSubmitPaymentException(paymentRequest.getId(), e.getMessage());
        }

        return mapper.toDTO(paymentRequest);
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

