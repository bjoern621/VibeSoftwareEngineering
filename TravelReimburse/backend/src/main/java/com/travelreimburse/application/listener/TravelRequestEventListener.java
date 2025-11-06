package com.travelreimburse.application.listener;

import com.travelreimburse.domain.event.TravelRequestStatusChangedEvent;
import com.travelreimburse.infrastructure.service.EmailNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Event Listener für Reiseantrag-Events
 * Reagiert auf Statusänderungen und triggert E-Mail-Benachrichtigungen
 */
@Component
public class TravelRequestEventListener {

    private static final Logger logger = LoggerFactory.getLogger(TravelRequestEventListener.class);
    private final EmailNotificationService emailService;

    public TravelRequestEventListener(EmailNotificationService emailService) {
        this.emailService = emailService;
    }

    /**
     * Behandelt Statusänderungs-Events und sendet E-Mail-Benachrichtigungen
     * 
     * @param event Das Event mit den Statusänderungs-Informationen
     */
    @EventListener
    public void handleStatusChange(TravelRequestStatusChangedEvent event) {
        logger.info("Event empfangen: Status-Änderung für Reiseantrag ID {} ({} -> {})",
                   event.getTravelRequest().getId(),
                   event.getOldStatus(),
                   event.getNewStatus());
        
        emailService.sendStatusChangeNotification(
            event.getTravelRequest(),
            event.getOldStatus(),
            event.getNewStatus()
        );
        
        logger.info("E-Mail-Benachrichtigung erfolgreich versendet für Reiseantrag ID {}",
                   event.getTravelRequest().getId());
    }
}

