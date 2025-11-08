package com.travelreimburse.domain.exception;

/**
 * Exception wenn ein Reiseziel nicht gefunden wurde
 * 
 * DDD: Domain-spezifische Exception
 */
public class DestinationNotFoundException extends RuntimeException {

    private final String countryCode;

    public DestinationNotFoundException(String countryCode) {
        super(String.format("Reiseziel mit Ländercode '%s' wurde nicht gefunden", countryCode));
        this.countryCode = countryCode;
    }

    public DestinationNotFoundException(Long id) {
        super(String.format("Reiseziel mit ID %d wurde nicht gefunden", id));
        this.countryCode = null;
    }

    public String getCountryCode() {
        return countryCode;
    }
}
