package com.rentacar.application.command;

/**
 * Command für Buchungsstornierung.
 * 
 * Kapselt alle nötigen Daten für den Use-Case "Buchung stornieren".
 * Command Pattern: Immutable, validiert sich selbst.
 */
public record CancelBookingCommand(
    Long bookingId,
    Long customerId, // null für Employee/Admin
    String reason
) {
    /**
     * Kompakter Konstruktor mit Validierung.
     */
    public CancelBookingCommand {
        if (bookingId == null) {
            throw new IllegalArgumentException("BookingId darf nicht null sein");
        }
        // customerId kann null sein (Employee/Admin haben keine customerId)
        // Reason ist optional (kann null sein)
    }
}

