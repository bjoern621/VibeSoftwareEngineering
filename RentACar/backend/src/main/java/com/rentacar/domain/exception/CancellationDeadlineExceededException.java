package com.rentacar.domain.exception;

import java.time.LocalDateTime;

/**
 * Exception wenn die Stornierungsfrist überschritten wurde.
 * 
 * Wird geworfen, wenn eine Buchung weniger als 24 Stunden vor der Abholung
 * storniert werden soll.
 */
public class CancellationDeadlineExceededException extends RuntimeException {
    
    private final Long bookingId;
    private final LocalDateTime pickupTime;

    public CancellationDeadlineExceededException(Long bookingId, LocalDateTime pickupTime, String message) {
        super(String.format("Stornierungsfrist für Buchung %d überschritten (Abholung: %s). %s",
            bookingId, pickupTime, message));
        this.bookingId = bookingId;
        this.pickupTime = pickupTime;
    }

    public Long getBookingId() { 
        return bookingId; 
    }
    
    public LocalDateTime getPickupTime() { 
        return pickupTime; 
    }
}
