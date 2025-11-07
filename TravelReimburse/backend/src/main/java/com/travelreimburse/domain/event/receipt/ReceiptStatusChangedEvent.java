package com.travelreimburse.domain.event.receipt;

import com.travelreimburse.domain.event.DomainEvent;
import com.travelreimburse.domain.model.ReceiptStatus;
import java.time.LocalDateTime;

/**
 * Domain event published when a Receipt changes its status.
 * This event triggers side-effects like email notifications.
 */
public record ReceiptStatusChangedEvent(
    Long receiptId,
    ReceiptStatus oldStatus,
    ReceiptStatus newStatus,
    Long changedByUserId,
    LocalDateTime occurredOn
) implements DomainEvent {
    
    public ReceiptStatusChangedEvent(Long receiptId, 
                                      ReceiptStatus oldStatus,
                                      ReceiptStatus newStatus, 
                                      Long changedByUserId) {
        this(receiptId, oldStatus, newStatus, changedByUserId, LocalDateTime.now());
    }
}

