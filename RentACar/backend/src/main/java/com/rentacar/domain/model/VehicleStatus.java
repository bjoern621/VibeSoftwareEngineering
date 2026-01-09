package com.rentacar.domain.model;

/**
 * Status eines Fahrzeugs im RentACar-System.
 * 
 * Definiert die möglichen Zustände eines Fahrzeugs während seines Lebenszyklus.
 */
public enum VehicleStatus {
    
    /**
     * Fahrzeug ist verfügbar und kann vermietet werden.
     */
    AVAILABLE("Verfügbar"),
    
    /**
     * Fahrzeug ist aktuell vermietet.
     */
    RENTED("Vermietet"),
    
    /**
     * Fahrzeug befindet sich in Wartung/Reparatur.
     */
    IN_MAINTENANCE("In Wartung"),
    
    /**
     * Fahrzeug ist außer Betrieb (z.B. wegen schwerer Schäden oder Ausmusterung).
     */
    OUT_OF_SERVICE("Außer Betrieb");
    
    private final String displayName;
    
    VehicleStatus(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * @return Anzeigename des Status
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Prüft, ob das Fahrzeug in diesem Status vermietet werden kann.
     * 
     * @return true wenn das Fahrzeug vermietet werden kann
     */
    public boolean isAvailableForRental() {
        return this == AVAILABLE;
    }
    
    /**
     * Prüft, ob das Fahrzeug zurückgegeben werden kann.
     * 
     * @return true wenn das Fahrzeug zurückgegeben werden kann
     */
    public boolean canBeReturned() {
        return this == RENTED;
    }
}
