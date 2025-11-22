package com.rentacar.domain.exception;

import com.rentacar.domain.model.BookingStatus;

/**
 * Exception für ungültige Statusübergänge bei Buchungen.
 * 
 * Wird geworfen, wenn versucht wird, einen Status-Übergang durchzuführen,
 * der gemäß der Business-Logik nicht erlaubt ist.
 */
public class BookingStatusTransitionException extends RuntimeException {
    
    private final Long bookingId;
    private final BookingStatus currentStatus;
    private final BookingStatus targetStatus;

    public BookingStatusTransitionException(Long bookingId, BookingStatus currentStatus, 
                                           BookingStatus targetStatus, String message) {
        super(String.format("Ungültiger Statusübergang für Buchung %d: %s -> %s. %s",
            bookingId, currentStatus, targetStatus, message));
        this.bookingId = bookingId;
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }

    public Long getBookingId() { 
        return bookingId; 
    }
    
    public BookingStatus getCurrentStatus() { 
        return currentStatus; 
    }
    
    public BookingStatus getTargetStatus() { 
        return targetStatus; 
    }
}
