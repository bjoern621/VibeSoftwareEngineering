package com.rentacar.domain.exception;

/**
 * Exception wenn Führerscheinnummer bereits existiert.
 */
public class DuplicateDriverLicenseException extends RuntimeException {
    public DuplicateDriverLicenseException(String licenseNumber) {
        super("Führerscheinnummer bereits registriert: " + licenseNumber);
    }
}
