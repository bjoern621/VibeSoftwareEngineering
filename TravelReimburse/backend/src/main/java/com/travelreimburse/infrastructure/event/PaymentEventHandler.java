package com.travelreimburse.infrastructure.event;

import com.travelreimburse.application.service.ArchivingService;
import com.travelreimburse.domain.event.PaymentFailedEvent;
import com.travelreimburse.domain.event.PaymentSuccessEvent;
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
 *
 * Nach erfolgreicher Zahlung:
 *  1. TravelRequest.status = PAID
 *  2. ArchivingService archiviert automatisch
 */
@Component
public class PaymentEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(PaymentEventHandler.class);

    private final TravelRequestRepository travelRequestRepository;
    private final ArchivingService archivingService;

    public PaymentEventHandler(
            TravelRequestRepository travelRequestRepository,
            ArchivingService archivingService) {
        this.travelRequestRepository = travelRequestRepository;
        this.archivingService = archivingService;
    }

    /**
     * Listener für PaymentSuccessEvent
     * Setzt TravelRequest Status zu PAID und archiviert automatisch
     */
    @EventListener
    @Transactional
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        logger.info("PaymentSuccessEvent empfangen für TravelRequest: {}", event.travelRequestId());

        var travelRequest = travelRequestRepository.findById(event.travelRequestId());
        if (travelRequest.isPresent()) {
            TravelRequest request = travelRequest.get();

            // Validiere Status
            if (request.getStatus() == TravelRequestStatus.APPROVED) {
                // Setze Status zu PAID
                request.pay();

                // Speichere
                travelRequestRepository.save(request);

                logger.info("TravelRequest {} wurde zu PAID gesetzt", event.travelRequestId());

                // Archiviere automatisch nach erfolgreicher Zahlung
                try {
                    archivingService.archiveAfterPaymentSuccess(event.travelRequestId());
                    logger.info("TravelRequest {} wurde automatisch archiviert", event.travelRequestId());
                } catch (Exception e) {
                    logger.error("Fehler beim automatischen Archivieren von TravelRequest {}: {}",
                        event.travelRequestId(), e.getMessage(), e);
                    // Archivierung ist nicht kritisch - Payment war erfolgreich
                }
            } else {
                logger.warn("TravelRequest {} hat Status {}, kann nicht zu PAID gesetzt werden",
                    event.travelRequestId(), request.getStatus());
            }
        } else {
            logger.error("TravelRequest nicht gefunden: {}", event.travelRequestId());
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

