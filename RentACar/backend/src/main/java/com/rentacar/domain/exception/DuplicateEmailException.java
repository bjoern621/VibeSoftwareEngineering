package com.rentacar.domain.exception;

/**
 * Exception wenn E-Mail bereits existiert.
 */
public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String email) {
        super("E-Mail-Adresse bereits registriert: " + email);
    }
}
