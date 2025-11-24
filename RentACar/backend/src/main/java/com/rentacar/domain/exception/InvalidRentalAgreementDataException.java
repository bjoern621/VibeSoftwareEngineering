package com.rentacar.domain.exception;

/**
 * Exception für ungültige Daten in einem Mietvertrag.
 */
public class InvalidRentalAgreementDataException extends RuntimeException {
    public InvalidRentalAgreementDataException(String message) {
        super(message);
    }
}
