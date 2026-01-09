package com.rentacar.domain.exception;

/**
 * Exception wenn Kunde nicht gefunden wurde.
 */
public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(Long id) {
        super("Kunde nicht gefunden mit ID: " + id);
    }

    public CustomerNotFoundException(String email) {
        super("Kunde nicht gefunden mit E-Mail: " + email);
    }

    public CustomerNotFoundException(String identifier, String value) {
        super("Kunde nicht gefunden mit " + identifier + ": " + value);
    }
}
