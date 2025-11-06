package com.travelreimburse.infrastructure.external.exrat;

/**
 * Exception für Fehler bei der ExRat-API-Kommunikation
 */
public class ExRatClientException extends RuntimeException {

    public ExRatClientException(String message) {
        super(message);
    }

    public ExRatClientException(String message, Throwable cause) {
        super(message, cause);
    }
}

