package com.rentacar.domain.exception;

/**
 * Exception für ungültige Preisdaten.
 * 
 * Wird geworfen, wenn Validierungsregeln bei Preisberechnungen verletzt werden.
 */
public class InvalidPricingDataException extends RuntimeException {

    private final String field;

    public InvalidPricingDataException(String message) {
        super(message);
        this.field = null;
    }

    public InvalidPricingDataException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
