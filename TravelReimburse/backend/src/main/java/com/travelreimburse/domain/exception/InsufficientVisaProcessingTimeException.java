package com.travelreimburse.domain.exception;

/**
 * Exception wenn Reise nicht genug Vorlaufzeit für Visa-Beantragung hat
 * 
 * DDD: Domain-spezifische Exception
 */
public class InsufficientVisaProcessingTimeException extends RuntimeException {

    private final String countryCode;
    private final long requiredDays;
    private final long availableDays;

    public InsufficientVisaProcessingTimeException(String countryCode, long requiredDays, long availableDays) {
        super(String.format(
            "Unzureichende Vorlaufzeit für Visum-Beantragung nach %s: " +
            "Erforderlich: %d Tage, Verfügbar: %d Tage",
            countryCode, requiredDays, availableDays
        ));
        this.countryCode = countryCode;
        this.requiredDays = requiredDays;
        this.availableDays = availableDays;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public long getRequiredDays() {
        return requiredDays;
    }

    public long getAvailableDays() {
        return availableDays;
    }
}
