package com.travelreimburse.domain.exception;

/**
 * Basis-Exception für alle Domain-spezifischen Fehler
 * DDD: Business-Fehler gehören in den Domain Layer
 */
public abstract class DomainException extends RuntimeException {
    
    protected DomainException(String message) {
        super(message);
    }
    
    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
