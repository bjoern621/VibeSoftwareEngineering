package com.rentacar.domain.exception;

/**
 * Exception für ungültige Kilometerstände.
 * 
 * Wird geworfen, wenn ein Kilometerstand negativ oder null ist,
 * oder wenn ein neuer Kilometerstand kleiner als der aktuelle ist.
 * 
 * Domain-spezifische Exception gemäß DDD-Prinzipien.
 */
public class InvalidMileageException extends RuntimeException {
    
    private final Integer invalidValue;
    
    /**
     * Erstellt eine neue InvalidMileageException.
     * 
     * @param message die Fehlermeldung
     */
    public InvalidMileageException(String message) {
        super(message);
        this.invalidValue = null;
    }
    
    /**
     * Erstellt eine neue InvalidMileageException mit dem ungültigen Wert.
     * 
     * @param message die Fehlermeldung
     * @param invalidValue der ungültige Kilometerstand
     */
    public InvalidMileageException(String message, Integer invalidValue) {
        super(message);
        this.invalidValue = invalidValue;
    }
    
    /**
     * @return der ungültige Kilometerstand
     */
    public Integer getInvalidValue() {
        return invalidValue;
    }
}
