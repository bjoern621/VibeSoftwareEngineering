package com.rentacar.domain.exception;

/**
 * Exception für ungültige Filialdaten.
 * 
 * Wird geworfen, wenn Validierungsregeln bei Filial-Operationen verletzt werden.
 */
public class InvalidBranchDataException extends RuntimeException {

    private final String field;

    public InvalidBranchDataException(String message) {
        super(message);
        this.field = null;
    }

    public InvalidBranchDataException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
