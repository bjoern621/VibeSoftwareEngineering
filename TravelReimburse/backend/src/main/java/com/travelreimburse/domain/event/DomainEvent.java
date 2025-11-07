package com.travelreimburse.domain.event;

import java.time.LocalDateTime;

/**
 * Marker interface for all domain events.
 * Domain events represent something that happened in the domain that
 * domain experts care about.
 */
public interface DomainEvent {

    /**
     * @return Timestamp when the event occurred
     */
    default LocalDateTime occurredOn() {
        return LocalDateTime.now();
    }
}

