package com.concertcomparison.domain.exception;

/**
 * Domain Exception: Wird geworfen, wenn versucht wird, einen nicht-reservierten Seat zu verkaufen.
 * 
 * Anwendungsfall:
 * - User versucht, einen Seat zu kaufen, der nicht (mehr) reserviert ist.
 * - Hold-Zeit ist abgelaufen und Seat wurde zwischenzeitlich freigegeben.
 * - Ungültige Reservation-ID beim Checkout.
 */
public class SeatNotHeldException extends RuntimeException {
    
    /**
     * Erstellt eine neue SeatNotHeldException mit der angegebenen Fehlermeldung.
     * 
     * @param message Beschreibung des Fehlers (z.B. "Seat A-12 muss zuerst reserviert werden")
     */
    public SeatNotHeldException(String message) {
        super(message);
    }
    
    /**
     * Erstellt eine neue SeatNotHeldException mit Fehlermeldung und Ursache.
     * 
     * @param message Beschreibung des Fehlers
     * @param cause Ursprüngliche Exception
     */
    public SeatNotHeldException(String message, Throwable cause) {
        super(message, cause);
    }
}
