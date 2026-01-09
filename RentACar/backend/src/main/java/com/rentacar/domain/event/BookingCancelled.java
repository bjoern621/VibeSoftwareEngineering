package com.rentacar.domain.event;

import java.time.LocalDateTime;

/**
 * Domain Event: Wird publiziert, wenn eine Buchung storniert wurde.
 * 
 * Erlaubt lose gekoppelte Reaktionen:
 * - Vehicle verfügbar machen
 * - E-Mail versenden
 * - Audit-Log schreiben (NFR5)
 * 
 * Event-Driven DDD: Aggregate kommunizieren über Events, nicht über direkte Referenzen.
 */
public class BookingCancelled {
    
    private final Long bookingId;
    private final Long customerId;
    private final Long vehicleId;
    private final String customerEmail;
    private final String customerName;
    private final String cancellationReason;
    private final LocalDateTime cancelledAt;
    
    public BookingCancelled(
        Long bookingId,
        Long customerId,
        Long vehicleId,
        String customerEmail,
        String customerName,
        String cancellationReason,
        LocalDateTime cancelledAt
    ) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.vehicleId = vehicleId;
        this.customerEmail = customerEmail;
        this.customerName = customerName;
        this.cancellationReason = cancellationReason;
        this.cancelledAt = cancelledAt;
    }
    
    // Getter
    
    public Long getBookingId() {
        return bookingId;
    }
    
    public Long getCustomerId() {
        return customerId;
    }
    
    public Long getVehicleId() {
        return vehicleId;
    }
    
    public String getCustomerEmail() {
        return customerEmail;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public String getCancellationReason() {
        return cancellationReason;
    }
    
    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }
    
    @Override
    public String toString() {
        return "BookingCancelled{" +
                "bookingId=" + bookingId +
                ", customerId=" + customerId +
                ", vehicleId=" + vehicleId +
                ", cancelledAt=" + cancelledAt +
                '}';
    }
}

