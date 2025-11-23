package com.rentacar.infrastructure.persistence;

import com.rentacar.domain.model.LicensePlate;
import com.rentacar.domain.model.Vehicle;
import com.rentacar.domain.model.VehicleStatus;
import com.rentacar.domain.model.VehicleType;
import com.rentacar.domain.repository.VehicleRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    @org.springframework.data.jpa.repository.Query("SELECT v FROM Vehicle v " +
           "JOIN v.branch b " +
           "WHERE v.status = 'AVAILABLE' " +
           "AND (:type IS NULL OR v.vehicleType = :type) " +
           "AND (:location IS NULL OR LOWER(b.address) LIKE LOWER(CONCAT('%', :location, '%')) OR LOWER(b.name) LIKE LOWER(CONCAT('%', :location, '%'))) " +
           "AND NOT EXISTS (" +
           "  SELECT bk FROM Booking bk " +
           "  WHERE bk.vehicle = v " +
           "  AND bk.status NOT IN ('CANCELLED', 'EXPIRED') " +
           "  AND bk.pickupDateTime < :to " +
           "  AND bk.returnDateTime > :from " +
           ")")
    @Override
    List<Vehicle> findAvailableVehicles(@org.springframework.data.repository.query.Param("from") java.time.LocalDateTime from,
                                        @org.springframework.data.repository.query.Param("to") java.time.LocalDateTime to,
                                        @org.springframework.data.repository.query.Param("type") VehicleType type,
                                        @org.springframework.data.repository.query.Param("location") String location);

    @Override
    @Query("SELECT v FROM Vehicle v WHERE v.vehicleType = :type AND v.status = 'AVAILABLE' " +
           "AND NOT EXISTS (SELECT b FROM Booking b WHERE b.vehicle = v " +
           "AND b.status NOT IN ('CANCELLED', 'EXPIRED') " +
           "AND ((b.pickupDateTime < :end) AND (b.returnDateTime > :start)))")
    List<Vehicle> findAvailableVehicles(@Param("type") VehicleType type, 
                                        @Param("start") LocalDateTime start, 
                                        @Param("end") LocalDateTime end);
}
