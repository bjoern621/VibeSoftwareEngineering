package com.rentacar.domain.exception;

/**
 * Exception wenn eine Buchung nicht gefunden wurde.
 * 
 * Wird geworfen, wenn eine Buchungs-ID nicht in der Datenbank existiert.
 */
public class BookingNotFoundException extends RuntimeException {
    
    private final Long bookingId;

    public BookingNotFoundException(Long bookingId) {
        super("Buchung mit ID " + bookingId + " nicht gefunden");
        this.bookingId = bookingId;
    }

    public Long getBookingId() { 
        return bookingId; 
    }
}
