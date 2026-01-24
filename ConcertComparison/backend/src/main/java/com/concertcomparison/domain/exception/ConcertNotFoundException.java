package com.concertcomparison.domain.exception;

/**
 * Domain Exception: Wird geworfen, wenn ein Concert nicht gefunden wird.
 * 
 * Anwendungsfall:
 * - User versucht, Details eines nicht-existierenden Konzerts abzurufen
 * - Versuch, Seats für ein nicht-existierendes Konzert zu reservieren
 */
public class ConcertNotFoundException extends ResourceNotFoundException {
    
    /**
     * Erstellt eine neue ConcertNotFoundException mit der angegebenen Concert-ID.
     * 
     * @param concertId ID des nicht gefundenen Konzerts
     */
    public ConcertNotFoundException(Long concertId) {
        super(String.format("Das Konzert mit ID %d wurde nicht gefunden", concertId), "Concert", concertId.toString());
    }

    /**
     * Erstellt eine neue ConcertNotFoundException mit custom Fehlermeldung.
     * 
     * @param message Benutzerfreundliche Fehlermeldung
     */
    public ConcertNotFoundException(String message) {
        super(message, "Concert", null);
    }
}
