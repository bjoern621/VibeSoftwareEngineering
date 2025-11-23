package com.rentacar.domain.repository;

import com.rentacar.domain.model.LicensePlate;
import com.rentacar.domain.model.Vehicle;
import com.rentacar.domain.model.VehicleStatus;
import com.rentacar.domain.model.VehicleType;
import java.util.List;
import java.util.Optional;

/**
 * Repository Interface für Vehicle (Fahrzeug).
 * 
 * Port-Interface im Sinne der Hexagonalen Architektur.
 * Wird in der Infrastructure-Schicht implementiert.
 */
public interface VehicleRepository {
    
    /**
     * Speichert ein Fahrzeug.
     * 
     * @param vehicle das zu speichernde Fahrzeug
     * @return das gespeicherte Fahrzeug
     */
    Vehicle save(Vehicle vehicle);
    
    /**
     * Findet ein Fahrzeug anhand seiner ID.
     * 
     * @param id die ID des Fahrzeugs
     * @return Optional mit dem Fahrzeug, falls vorhanden
     */
    Optional<Vehicle> findById(Long id);
    
    /**
     * Findet ein Fahrzeug anhand seines Kennzeichens.
     * 
     * @param licensePlate das Kennzeichen
     * @return Optional mit dem Fahrzeug, falls vorhanden
     */
    Optional<Vehicle> findByLicensePlate(LicensePlate licensePlate);
    
    /**
     * Findet alle Fahrzeuge.
     * 
     * @return Liste aller Fahrzeuge
     */
    List<Vehicle> findAll();
    
    /**
     * Findet alle Fahrzeuge mit einem bestimmten Status.
     * 
     * @param status der Status
     * @return Liste der Fahrzeuge mit diesem Status
     */
    List<Vehicle> findByStatus(VehicleStatus status);
    
    /**
     * Findet alle Fahrzeuge eines bestimmten Typs.
     * 
     * @param vehicleType der Fahrzeugtyp
     * @return Liste der Fahrzeuge dieses Typs
     */
    List<Vehicle> findByVehicleType(VehicleType vehicleType);
    
    /**
     * Findet alle Fahrzeuge einer bestimmten Filiale.
     * 
     * @param branchId die ID der Filiale
     * @return Liste der Fahrzeuge dieser Filiale
     */
    List<Vehicle> findByBranchId(Long branchId);
    
    /**
     * Findet alle verfügbaren Fahrzeuge eines bestimmten Typs in einer Filiale.
     * 
     * @param vehicleType der Fahrzeugtyp
     * @param branchId die ID der Filiale
     * @return Liste der verfügbaren Fahrzeuge
     */
    List<Vehicle> findAvailableByTypeAndBranch(VehicleType vehicleType, Long branchId);
    
    /**
     * Findet verfügbare Fahrzeuge basierend auf Suchkriterien.
     * 
     * @param from Startzeitpunkt der Buchung
     * @param to Endzeitpunkt der Buchung
     * @param type Fahrzeugtyp (optional)
     * @param location Standort/Stadt (optional)
     * @return Liste der verfügbaren Fahrzeuge
     */
    List<Vehicle> findAvailableVehicles(java.time.LocalDateTime from, java.time.LocalDateTime to, VehicleType type, String location);
    
    /**
     * Löscht ein Fahrzeug.
     * 
     * @param vehicle das zu löschende Fahrzeug
     */
    void delete(Vehicle vehicle);
    
    /**
     * Prüft, ob ein Fahrzeug mit dem gegebenen Kennzeichen existiert.
     * 
     * @param licensePlate das Kennzeichen
     * @return true wenn ein Fahrzeug mit diesem Kennzeichen existiert
     */
    boolean existsByLicensePlate(LicensePlate licensePlate);
}
