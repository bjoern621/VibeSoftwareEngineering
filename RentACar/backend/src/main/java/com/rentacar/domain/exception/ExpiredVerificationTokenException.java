package com.rentacar.domain.exception;

/**
 * Exception, die geworfen wird, wenn ein Verifikations-Token abgelaufen ist.
 */
public class ExpiredVerificationTokenException extends RuntimeException {

    public ExpiredVerificationTokenException(String token) {
        super("Verifikations-Token ist abgelaufen: " + token);
    }

    public ExpiredVerificationTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
