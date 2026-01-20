package com.concertcomparison.domain.exception;

/**
 * Domain Exception: Wird geworfen, wenn eine Reservation abgelaufen ist.
 * 
 * Anwendungsfall:
 * - User versucht, ein Ticket basierend auf einer abgelaufenen Reservation zu kaufen.
 * - expiresAt < LocalDateTime.now()
 */
public class ReservationExpiredException extends RuntimeException {
    
    /**
     * Erstellt eine neue ReservationExpiredException mit der angegebenen Fehlermeldung.
     * 
     * @param message Beschreibung des Fehlers (z.B. "Reservation 123 ist abgelaufen")
     */
    public ReservationExpiredException(String message) {
        super(message);
    }
    
    /**
     * Erstellt eine neue ReservationExpiredException mit Fehlermeldung und Ursache.
     * 
     * @param message Beschreibung des Fehlers
     * @param cause Ursprüngliche Exception
     */
    public ReservationExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
