package com.travelreimburse.domain.exception;

/**
 * Exception wenn ungültiger Ländercode verwendet wird
 * 
 * DDD: Domain-spezifische Exception
 */
public class InvalidCountryCodeException extends RuntimeException {

    private final String invalidCode;

    public InvalidCountryCodeException(String invalidCode, String reason) {
        super(String.format("Ungültiger Ländercode '%s': %s", invalidCode, reason));
        this.invalidCode = invalidCode;
    }

    public InvalidCountryCodeException(String invalidCode) {
        this(invalidCode, "Ländercode muss 2 Großbuchstaben sein (z.B. DE, US, FR)");
    }

    public String getInvalidCode() {
        return invalidCode;
    }
}
