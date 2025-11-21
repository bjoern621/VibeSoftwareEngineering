package com.rentacar.domain.exception;

/**
 * Exception für ungültiges E-Mail-Format.
 */
public class InvalidEmailException extends RuntimeException {
    public InvalidEmailException(String email) {
        super("Ungültiges E-Mail-Format: " + email);
    }
}
