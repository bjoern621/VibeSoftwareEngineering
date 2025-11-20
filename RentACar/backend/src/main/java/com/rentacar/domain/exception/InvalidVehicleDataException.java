package com.rentacar.domain.exception;

/**
 * Exception für ungültige Fahrzeugdaten.
 * 
 * Wird geworfen, wenn Validierungen für Fahrzeugattribute fehlschlagen
 * (z.B. leere Marke, ungültiges Baujahr, etc.).
 * 
 * Domain-spezifische Exception gemäß DDD-Prinzipien.
 */
public class InvalidVehicleDataException extends RuntimeException {
    
    private final String fieldName;
    private final Object invalidValue;
    
    /**
     * Erstellt eine neue InvalidVehicleDataException.
     * 
     * @param message die Fehlermeldung
     */
    public InvalidVehicleDataException(String message) {
        super(message);
        this.fieldName = null;
        this.invalidValue = null;
    }
    
    /**
     * Erstellt eine neue InvalidVehicleDataException mit Feldname und ungültigem Wert.
     * 
     * @param message die Fehlermeldung
     * @param fieldName der Name des ungültigen Feldes
     * @param invalidValue der ungültige Wert
     */
    public InvalidVehicleDataException(String message, String fieldName, Object invalidValue) {
        super(message);
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
    }
    
    /**
     * @return der Name des ungültigen Feldes
     */
    public String getFieldName() {
        return fieldName;
    }
    
    /**
     * @return der ungültige Wert
     */
    public Object getInvalidValue() {
        return invalidValue;
    }
}
