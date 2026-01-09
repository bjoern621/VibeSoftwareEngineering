package com.rentacar.domain.exception;

/**
 * Exception, die geworfen wird, wenn ein Verifikations-Token ungültig ist.
 */
public class InvalidVerificationTokenException extends RuntimeException {

    public InvalidVerificationTokenException(String token) {
        super("Ungültiger Verifikations-Token: " + token);
    }

    public InvalidVerificationTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
