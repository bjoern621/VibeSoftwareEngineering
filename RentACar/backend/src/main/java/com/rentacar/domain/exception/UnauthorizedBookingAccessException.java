package com.rentacar.domain.exception;

/**
 * Exception wenn ein Benutzer versucht, eine Buchung zu stornieren,
 * für die er keine Berechtigung hat.
 * 
 * Business Rule: Nur der Buchungseigentümer oder Mitarbeiter/Admin dürfen stornieren.
 */
public class UnauthorizedBookingAccessException extends RuntimeException {
    
    private final Long bookingId;
    private final Long customerId;
    
    public UnauthorizedBookingAccessException(Long bookingId, Long customerId) {
        super(String.format(
            "Kunde %d ist nicht berechtigt, Buchung %d zu stornieren", 
            customerId, 
            bookingId
        ));
        this.bookingId = bookingId;
        this.customerId = customerId;
    }
    
    public Long getBookingId() {
        return bookingId;
    }
    
    public Long getCustomerId() {
        return customerId;
    }
}

