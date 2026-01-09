package com.rentacar.domain.exception;

/**
 * Exception für ungültige Führerscheinnummer.
 */
public class InvalidDriverLicenseException extends RuntimeException {
    public InvalidDriverLicenseException(String message) {
        super("Ungültige Führerscheinnummer: " + message);
    }
}
