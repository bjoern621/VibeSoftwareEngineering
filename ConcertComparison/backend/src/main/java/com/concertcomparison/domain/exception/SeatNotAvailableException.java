package com.concertcomparison.domain.exception;

/**
 * Domain Exception: Wird geworfen, wenn versucht wird, einen nicht-verfügbaren Seat zu reservieren.
 * 
 * Anwendungsfall: 
 * - User versucht, einen bereits reservierten (HELD) oder verkauften (SOLD) Seat zu reservieren.
 * - Race Condition: Zwei gleichzeitige Requests versuchen, denselben Seat zu reservieren.
 */
public class SeatNotAvailableException extends RuntimeException {
    
    /**
     * Erstellt eine neue SeatNotAvailableException mit der angegebenen Fehlermeldung.
     * 
     * @param message Beschreibung des Fehlers (z.B. "Seat A-12 ist bereits reserviert")
     */
    public SeatNotAvailableException(String message) {
        super(message);
    }
    
    /**
     * Erstellt eine neue SeatNotAvailableException mit Fehlermeldung und Ursache.
     * 
     * @param message Beschreibung des Fehlers
     * @param cause Ursprüngliche Exception (z.B. OptimisticLockException)
     */
    public SeatNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
