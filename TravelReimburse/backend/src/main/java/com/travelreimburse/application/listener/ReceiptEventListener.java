package com.travelreimburse.application.listener;

import com.travelreimburse.domain.event.receipt.ReceiptStatusChangedEvent;
import com.travelreimburse.domain.model.Receipt;
import com.travelreimburse.domain.repository.ReceiptRepository;
import com.travelreimburse.infrastructure.service.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event listener for Receipt domain events.
 * Handles side-effects like email notifications.
 * DDD: Decouples email logic from business logic.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReceiptEventListener {

    private final EmailNotificationService emailService;
    private final ReceiptRepository repository;

    /**
     * Handle status change events by sending email notifications.
     * Runs asynchronously to avoid blocking the main transaction.
     */
    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onStatusChanged(ReceiptStatusChangedEvent event) {
        log.info("Processing ReceiptStatusChangedEvent: {} -> {}",
                 event.oldStatus(), event.newStatus());

        try {
            Receipt receipt = repository.findById(event.receiptId())
                .orElseThrow(() -> new IllegalStateException(
                    "Receipt not found: " + event.receiptId()));

            emailService.sendReceiptStatusChangeNotification(
                receipt,
                event.oldStatus(),
                event.newStatus()
            );

            log.info("Email notification sent for Receipt {}", event.receiptId());
        } catch (Exception e) {
            log.error("Failed to send email notification for Receipt {}",
                     event.receiptId(), e);
            // Don't throw - email failure shouldn't break business logic
        }
    }
}

