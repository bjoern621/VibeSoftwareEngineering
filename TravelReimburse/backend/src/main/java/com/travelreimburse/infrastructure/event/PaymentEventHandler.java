package com.travelreimburse.infrastructure.event;

import com.travelreimburse.domain.event.payment.PaymentFailedEvent;
import com.travelreimburse.domain.event.payment.PaymentSuccessEvent;
import com.travelreimburse.domain.model.TravelRequest;
import com.travelreimburse.domain.model.TravelRequestStatus;
import com.travelreimburse.domain.repository.TravelRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event Handler für Payment-Events.
 * Reagiert auf Payment-Success und Payment-Failed Events und aktualisiert den TravelRequest Status.
 * Nach erfolgreicher Zahlung:
 *  1. TravelRequest.status = PAID
 *
 * ⚠️ Archivierung ist NICHT mehr automatisch - wird separat gehandhabt!
 */
@Component
public class PaymentEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(PaymentEventHandler.class);

    private final TravelRequestRepository travelRequestRepository;

    public PaymentEventHandler(TravelRequestRepository travelRequestRepository) {
        this.travelRequestRepository = travelRequestRepository;
    }

    /**
     * Listener für PaymentSuccessEvent
     * Setzt TravelRequest Status zu PAID (wenn noch nicht PAID)
     */
    @EventListener
    @Transactional
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        logger.info("📢 PaymentSuccessEvent empfangen für TravelRequest: {}", event.travelRequestId());

        var travelRequestOpt = travelRequestRepository.findById(event.travelRequestId());
        if (travelRequestOpt.isEmpty()) {
            logger.error("❌ TravelRequest nicht gefunden: {}", event.travelRequestId());
            return;
        }

        TravelRequest travelRequest = travelRequestOpt.get();
        TravelRequestStatus currentStatus = travelRequest.getStatus();

        // ✅ Idempotenz-Check: Wenn bereits PAID, überspringe
        if (currentStatus == TravelRequestStatus.PAID) {
            logger.info("ℹ️ TravelRequest {} ist bereits PAID - Event wird ignoriert", event.travelRequestId());
            return;
        }

        // ✅ Validiere dass Status APPROVED ist
        if (currentStatus == TravelRequestStatus.APPROVED) {
            travelRequest.pay();
            travelRequestRepository.save(travelRequest);

            logger.info("✅ TravelRequest {} Status: {} → PAID", event.travelRequestId(), currentStatus);

            // ⚠️ KEINE automatische Archivierung!
            // Archivierung erfolgt separat via manuellen Aufruf

        } else {
            logger.warn("⚠️ TravelRequest {} hat Status {}, erwartet APPROVED - Event wird ignoriert",
                event.travelRequestId(), currentStatus);
        }
    }

    /**
     * Listener für PaymentFailedEvent
     * Sendet Benachrichtigung an Finance Team
     */
    @EventListener
    @Transactional(readOnly = true)
    public void handlePaymentFailed(PaymentFailedEvent event) {
        logger.error("PaymentFailedEvent empfangen für TravelRequest: {} - Grund: {}",
            event.travelRequestId(), event.reason());

        var travelRequest = travelRequestRepository.findById(event.travelRequestId());
        if (travelRequest.isPresent()) {
            logger.info("Benachrichtigung an Finance Team: Payment fehlgeschlagen für TravelRequest {}",
                event.travelRequestId());
        }
    }
}

