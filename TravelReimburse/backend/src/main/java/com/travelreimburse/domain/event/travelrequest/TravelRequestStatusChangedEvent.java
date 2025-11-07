package com.travelreimburse.domain.event.travelrequest;

import com.travelreimburse.domain.event.DomainEvent;
import com.travelreimburse.domain.model.TravelRequestStatus;
import java.time.LocalDateTime;

/**
 * Domain event published when a TravelRequest changes its status.
 * This event triggers side-effects like email notifications.
 */
public record TravelRequestStatusChangedEvent(
    Long travelRequestId,
    TravelRequestStatus oldStatus,
    TravelRequestStatus newStatus,
    Long changedByUserId,
    LocalDateTime occurredOn
) implements DomainEvent {
    
    public TravelRequestStatusChangedEvent(Long travelRequestId, 
                                            TravelRequestStatus oldStatus,
                                            TravelRequestStatus newStatus, 
                                            Long changedByUserId) {
        this(travelRequestId, oldStatus, newStatus, changedByUserId, LocalDateTime.now());
    }
}

