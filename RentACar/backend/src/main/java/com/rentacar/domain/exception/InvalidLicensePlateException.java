package com.rentacar.domain.exception;

/**
 * Exception für ungültige Kennzeichen.
 * 
 * Wird geworfen, wenn ein Kennzeichen nicht dem erwarteten Format entspricht
 * oder null/leer ist.
 * 
 * Domain-spezifische Exception gemäß DDD-Prinzipien.
 */
public class InvalidLicensePlateException extends RuntimeException {
    
    private final String invalidValue;
    
    /**
     * Erstellt eine neue InvalidLicensePlateException.
     * 
     * @param invalidValue der ungültige Kennzeichen-Wert
     */
    public InvalidLicensePlateException(String invalidValue) {
        super("Ungültiges Kennzeichen-Format: " + invalidValue + ". Erwartetes Format: XX-YY 1234");
        this.invalidValue = invalidValue;
    }
    
    /**
     * Erstellt eine neue InvalidLicensePlateException mit benutzerdefinierter Nachricht.
     * 
     * @param message die Fehlermeldung
     * @param invalidValue der ungültige Kennzeichen-Wert
     */
    public InvalidLicensePlateException(String message, String invalidValue) {
        super(message);
        this.invalidValue = invalidValue;
    }
    
    /**
     * @return der ungültige Kennzeichen-Wert
     */
    public String getInvalidValue() {
        return invalidValue;
    }
}
