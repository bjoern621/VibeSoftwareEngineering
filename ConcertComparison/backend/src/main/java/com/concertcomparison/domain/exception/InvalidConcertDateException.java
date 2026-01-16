package com.concertcomparison.domain.exception;

/**
 * Exception die geworfen wird, wenn ein Konzertdatum ungültig ist.
 * 
 * Business Rule: Konzerte können nur für zukünftige Daten angelegt werden.
 */
public class InvalidConcertDateException extends RuntimeException {
    
    public InvalidConcertDateException(String message) {
        super(message);
    }
    
    public InvalidConcertDateException(String message, Throwable cause) {
        super(message, cause);
    }
}
