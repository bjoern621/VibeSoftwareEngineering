package com.travelreimburse.application.listener;

import com.travelreimburse.domain.event.ReceiptStatusChangedEvent;
import com.travelreimburse.infrastructure.service.EmailNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Event Listener für Beleg-Events
 * Reagiert auf Statusänderungen und triggert E-Mail-Benachrichtigungen
 */
@Component
public class ReceiptEventListener {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptEventListener.class);
    private final EmailNotificationService emailService;

    public ReceiptEventListener(EmailNotificationService emailService) {
        this.emailService = emailService;
    }

    /**
     * Behandelt Statusänderungs-Events für Belege und sendet E-Mail-Benachrichtigungen
     * 
     * @param event Das Event mit den Statusänderungs-Informationen
     */
    @EventListener
    public void handleStatusChange(ReceiptStatusChangedEvent event) {
        logger.info("Event empfangen: Status-Änderung für Beleg ID {} ({} -> {})",
                   event.getReceipt().getId(),
                   event.getOldStatus(),
                   event.getNewStatus());
        
        emailService.sendReceiptStatusChangeNotification(
            event.getReceipt(),
            event.getOldStatus(),
            event.getNewStatus()
        );
        
        logger.info("E-Mail-Benachrichtigung erfolgreich versendet für Beleg ID {}",
                   event.getReceipt().getId());
    }
}

