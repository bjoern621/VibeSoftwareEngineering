package com.rentacar.domain.exception;

/**
 * Exception für den Fall, dass ein Fahrzeug nicht gefunden wurde.
 * 
 * Domain-spezifische Exception gemäß DDD-Prinzipien.
 */
public class VehicleNotFoundException extends RuntimeException {
    
    private final Long vehicleId;
    
    /**
     * Erstellt eine neue VehicleNotFoundException.
     * 
     * @param vehicleId die ID des nicht gefundenen Fahrzeugs
     */
    public VehicleNotFoundException(Long vehicleId) {
        super("Fahrzeug mit ID " + vehicleId + " wurde nicht gefunden");
        this.vehicleId = vehicleId;
    }
    
    /**
     * Erstellt eine neue VehicleNotFoundException mit einer benutzerdefinierten Nachricht.
     * 
     * @param message die Fehlermeldung
     */
    public VehicleNotFoundException(String message) {
        super(message);
        this.vehicleId = null;
    }
    
    public Long getVehicleId() {
        return vehicleId;
    }
}
