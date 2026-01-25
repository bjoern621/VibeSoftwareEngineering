package com.concertcomparison.domain.exception;

/**
 * Domain Exception: Wird geworfen, wenn eine Reservation/Hold nicht gefunden wird.
 * 
 * Anwendungsfall:
 * - User versucht, eine nicht-existierende Reservation zu kaufen
 * - Versuch, eine Reservation zu stornieren, die nicht existiert
 */
public class ReservationNotFoundException extends ResourceNotFoundException {
    
    /**
     * Erstellt eine neue ReservationNotFoundException mit der angegebenen Reservation-ID.
     * 
     * @param reservationId ID der nicht gefundenen Reservation
     */
    public ReservationNotFoundException(Long reservationId) {
        super(String.format("Die Reservierung mit ID %d wurde nicht gefunden", reservationId), "Reservation", reservationId.toString());
    }

    /**
     * Erstellt eine neue ReservationNotFoundException mit custom Fehlermeldung.
     * 
     * @param message Benutzerfreundliche Fehlermeldung
     */
    public ReservationNotFoundException(String message) {
        super(message, "Reservation", null);
    }
}
