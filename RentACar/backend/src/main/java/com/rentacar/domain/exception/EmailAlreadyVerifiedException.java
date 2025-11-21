package com.rentacar.domain.exception;

/**
 * Exception, die geworfen wird, wenn eine E-Mail-Adresse bereits verifiziert wurde.
 */
public class EmailAlreadyVerifiedException extends RuntimeException {

    public EmailAlreadyVerifiedException(String email) {
        super("E-Mail-Adresse '" + email + "' wurde bereits verifiziert");
    }

    public EmailAlreadyVerifiedException(String message, Throwable cause) {
        super(message, cause);
    }
}
