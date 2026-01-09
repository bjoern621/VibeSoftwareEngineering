package com.rentacar.domain.exception;

/**
 * Exception für ungültige Adressdaten.
 * 
 * Wird geworfen, wenn Validierungsregeln bei Adress-Value-Objects verletzt werden.
 */
public class InvalidAddressException extends RuntimeException {

    private final String field;

    public InvalidAddressException(String message) {
        super(message);
        this.field = null;
    }

    public InvalidAddressException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
