package com.rentacar.domain.exception;

/**
 * Exception für ungültige Buchungsdaten.
 * 
 * Wird geworfen, wenn Validierungsregeln bei der Buchungserstellung verletzt werden.
 */
public class InvalidBookingDataException extends RuntimeException {

    private final String field;

    public InvalidBookingDataException(String message) {
        super(message);
        this.field = null;
    }

    public InvalidBookingDataException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
