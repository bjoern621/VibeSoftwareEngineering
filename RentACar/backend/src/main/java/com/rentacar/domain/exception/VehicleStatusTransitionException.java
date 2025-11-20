package com.rentacar.domain.exception;

import com.rentacar.domain.model.VehicleStatus;

/**
 * Exception für ungültige Statusübergänge bei Fahrzeugen.
 * 
 * Wird geworfen, wenn versucht wird, ein Fahrzeug in einen ungültigen Status
 * zu versetzen (z.B. ein vermietetes Fahrzeug auszumustern).
 * 
 * Domain-spezifische Exception gemäß DDD-Prinzipien.
 */
public class VehicleStatusTransitionException extends RuntimeException {
    
    private final Long vehicleId;
    private final VehicleStatus currentStatus;
    private final VehicleStatus targetStatus;
    
    /**
     * Erstellt eine neue VehicleStatusTransitionException.
     * 
     * @param vehicleId die ID des betroffenen Fahrzeugs
     * @param currentStatus der aktuelle Status
     * @param targetStatus der gewünschte Zielstatus
     */
    public VehicleStatusTransitionException(Long vehicleId, VehicleStatus currentStatus, VehicleStatus targetStatus) {
        super(String.format(
            "Fahrzeug mit ID %d kann nicht von Status '%s' in Status '%s' überführt werden",
            vehicleId, currentStatus, targetStatus
        ));
        this.vehicleId = vehicleId;
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }
    
    /**
     * Erstellt eine neue VehicleStatusTransitionException mit benutzerdefinierter Nachricht.
     * 
     * @param message die Fehlermeldung
     * @param currentStatus der aktuelle Status
     */
    public VehicleStatusTransitionException(String message, VehicleStatus currentStatus) {
        super(message);
        this.vehicleId = null;
        this.currentStatus = currentStatus;
        this.targetStatus = null;
    }
    
    /**
     * @return die Fahrzeug-ID
     */
    public Long getVehicleId() {
        return vehicleId;
    }
    
    /**
     * @return der aktuelle Status
     */
    public VehicleStatus getCurrentStatus() {
        return currentStatus;
    }
    
    /**
     * @return der Zielstatus
     */
    public VehicleStatus getTargetStatus() {
        return targetStatus;
    }
}
