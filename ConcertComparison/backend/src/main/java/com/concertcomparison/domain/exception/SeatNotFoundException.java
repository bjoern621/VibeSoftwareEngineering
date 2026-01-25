package com.concertcomparison.domain.exception;

/**
 * Domain Exception: Wird geworfen, wenn ein Seat nicht gefunden wird.
 * 
 * Anwendungsfall:
 * - User versucht, einen nicht-existierenden Seat zu reservieren
 * - Versuch, einen Seat zu aktualisieren, der nicht existiert
 */
public class SeatNotFoundException extends ResourceNotFoundException {
    
    /**
     * Erstellt eine neue SeatNotFoundException mit der angegebenen Seat-ID.
     * 
     * @param seatId ID des nicht gefundenen Seats
     */
    public SeatNotFoundException(Long seatId) {
        super(String.format("Der Sitzplatz mit ID %d wurde nicht gefunden", seatId), "Seat", seatId.toString());
    }

    /**
     * Erstellt eine neue SeatNotFoundException mit Seat-Nummer.
     * 
     * @param seatNumber Nummer des nicht gefundenen Seats (z.B. "A-12")
     */
    public SeatNotFoundException(String seatNumber) {
        super(String.format("Der Sitzplatz %s wurde nicht gefunden", seatNumber), "Seat", seatNumber);
    }

    /**
     * Erstellt eine neue SeatNotFoundException mit custom Fehlermeldung.
     * 
     * @param message Benutzerfreundliche Fehlermeldung
     */
    public SeatNotFoundException(String message, String seatId) {
        super(message, "Seat", seatId);
    }
}
