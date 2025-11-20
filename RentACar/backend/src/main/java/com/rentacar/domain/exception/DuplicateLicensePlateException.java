package com.rentacar.domain.exception;

/**
 * Exception für den Fall, dass ein Kennzeichen bereits existiert.
 * 
 * Domain-spezifische Exception gemäß DDD-Prinzipien.
 * Wird geworfen, wenn versucht wird, ein Fahrzeug mit einem bereits
 * vorhandenen Kennzeichen zu registrieren.
 */
public class DuplicateLicensePlateException extends RuntimeException {
    
    private final String licensePlate;
    
    /**
     * Erstellt eine neue DuplicateLicensePlateException.
     * 
     * @param licensePlate das doppelte Kennzeichen
     */
    public DuplicateLicensePlateException(String licensePlate) {
        super("Fahrzeug mit Kennzeichen '" + licensePlate + "' existiert bereits");
        this.licensePlate = licensePlate;
    }
    
    public String getLicensePlate() {
        return licensePlate;
    }
}
