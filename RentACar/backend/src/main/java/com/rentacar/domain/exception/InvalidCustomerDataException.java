package com.rentacar.domain.exception;

/**
 * Exception für ungültige Kundendaten.
 * 
 * Wird geworfen, wenn Validierungsregeln bei Kunden-Operationen verletzt werden.
 */
public class InvalidCustomerDataException extends RuntimeException {

    private final String field;

    public InvalidCustomerDataException(String message) {
        super(message);
        this.field = null;
    }

    public InvalidCustomerDataException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
