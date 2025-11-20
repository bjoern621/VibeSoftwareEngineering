package com.rentacar.infrastructure.persistence;

import com.rentacar.domain.model.LicensePlate;
import com.rentacar.domain.model.Vehicle;
import com.rentacar.domain.model.VehicleStatus;
import com.rentacar.domain.model.VehicleType;
import com.rentacar.domain.repository.VehicleRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA-Implementierung des VehicleRepository.
 * 
 * Spring Data JPA leitet die Implementierung automatisch ab.
 * Diese Schnittstelle befindet sich in der Infrastructure-Schicht
 * und implementiert das Port-Interface aus der Domain-Schicht.
 */
@Repository
public interface JpaVehicleRepository extends VehicleRepository, JpaRepository<Vehicle, Long> {
    
    // Alle Methoden werden automatisch von Spring Data JPA implementiert
    // basierend auf den Methodennamen und den Interfaces VehicleRepository und JpaRepository
    
    // Die folgenden Methoden aus VehicleRepository werden automatisch implementiert:
    // - save(Vehicle) -> von JpaRepository
    // - findById(Long) -> von JpaRepository
    // - findAll() -> von JpaRepository
    // - delete(Vehicle) -> von JpaRepository
    // - findByLicensePlate(LicensePlate) -> Query Derivation
    // - existsByLicensePlate(LicensePlate) -> Query Derivation
    // - findByStatus(VehicleStatus) -> Query Derivation
    // - findByVehicleType(VehicleType) -> Query Derivation
    // - findByBranchId(Long) -> Query Derivation
    // - findAvailableByTypeAndBranch(VehicleType, Long) -> Custom Query benötigt
    
    /**
     * Überschreibt die Methode aus VehicleRepository mit einer optimierten Query.
     * Findet alle verfügbaren Fahrzeuge eines bestimmten Typs in einer Filiale.
     */
    @Override
    default List<Vehicle> findAvailableByTypeAndBranch(VehicleType vehicleType, Long branchId) {
        return findByVehicleTypeAndBranchIdAndStatus(vehicleType, branchId, VehicleStatus.AVAILABLE);
    }
    
    /**
     * Hilfsmethode für die optimierte Suche.
     * Spring Data JPA leitet automatisch die Query ab.
     */
    List<Vehicle> findByVehicleTypeAndBranchIdAndStatus(VehicleType vehicleType, Long branchId, VehicleStatus status);
}
