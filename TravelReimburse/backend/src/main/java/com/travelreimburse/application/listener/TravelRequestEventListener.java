package com.travelreimburse.application.listener;

import com.travelreimburse.domain.event.travelrequest.TravelRequestStatusChangedEvent;
import com.travelreimburse.domain.model.TravelRequest;
import com.travelreimburse.domain.repository.TravelRequestRepository;
import com.travelreimburse.infrastructure.service.EmailNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

/**
 * Event listener for TravelRequest domain events.
 * Handles side-effects like email notifications.
 * DDD: Decouples email logic from business logic.
 */
@Component
public class TravelRequestEventListener {

    private static final Logger log = LoggerFactory.getLogger(TravelRequestEventListener.class);

    private final EmailNotificationService emailService;
    private final TravelRequestRepository repository;

    public TravelRequestEventListener(EmailNotificationService emailService,
                                       TravelRequestRepository repository) {
        this.emailService = emailService;
        this.repository = repository;
    }

    /**
     * Handle status change events by sending email notifications.
     * Runs AFTER COMMIT to ensure DB state is visible.
     * NOTE: @Async temporarily disabled for debugging
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStatusChanged(TravelRequestStatusChangedEvent event) {
        try {
            TravelRequest request = repository.findById(event.travelRequestId())
                .orElseThrow(() -> new IllegalStateException(
                    "TravelRequest not found: " + event.travelRequestId()));

            // Get employee email - for now using mock,
            String employeeEmail = "employee" + request.getEmployeeId() + "@company.com";

            emailService.sendStatusChangeNotification(
                request,
                event.oldStatus(),
                event.newStatus()
            );

        } catch (Exception e) {
            log.error("❌ Failed to send email notification for TravelRequest {}",
                     event.travelRequestId(), e);
            // Don't throw - email failure shouldn't break business logic
        }
    }
}

