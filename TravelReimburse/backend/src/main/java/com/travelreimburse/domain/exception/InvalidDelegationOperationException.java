package com.travelreimburse.domain.exception;

/**
 * Exception für ungültige Delegation-Operationen
 * 
 * Wird geworfen, wenn eine Business-Operation auf einer Delegation nicht erlaubt ist
 * (z.B. bereits widerrufene Delegation erneut widerrufen)
 */
public class InvalidDelegationOperationException extends RuntimeException {
    
    public InvalidDelegationOperationException(String message) {
        super(message);
    }
    
    public InvalidDelegationOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
