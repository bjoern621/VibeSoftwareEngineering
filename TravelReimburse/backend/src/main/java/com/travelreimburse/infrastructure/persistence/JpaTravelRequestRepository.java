package com.travelreimburse.infrastructure.persistence;

import com.travelreimburse.domain.model.TravelRequest;
import com.travelreimburse.domain.model.TravelRequestStatus;
import com.travelreimburse.domain.repository.TravelRequestRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * JPA Repository Implementierung für TravelRequest
 * Kombiniert Spring Data JPA mit dem Domain Repository Interface
 * 
 * Spring Data JPA generiert automatisch die Implementierung für:
 * - save(), findById(), findAll(), delete(), etc. (von JpaRepository)
 * - findByEmployeeId() (durch Methodennamen-Parsing)
 * 
 * DDD: Infrastructure Layer - technische Implementierung der Domain-Interfaces
 */
@Repository
public interface JpaTravelRequestRepository 
    extends JpaRepository<TravelRequest, Long>, TravelRequestRepository {
    
    /**
     * Spring Data JPA generiert automatisch die Query:
     * SELECT * FROM travel_requests WHERE employee_id = ?
     */
    @Override
    List<TravelRequest> findByEmployeeId(Long employeeId);

    /**
     * Spring Data JPA generiert automatisch die Query:
     * SELECT * FROM travel_requests WHERE status = 'SUBMITTED'
     * Diese Methode ist JPA-spezifisch und nicht im Domain-Interface
     */
    List<TravelRequest> findByStatus(TravelRequestStatus status);

    /**
     * Findet alle eingereichten Anträge (Implementierung für Domain-Interface)
     */
    @Override
    default List<TravelRequest> findPendingApprovals() {
        return findByStatus(TravelRequestStatus.SUBMITTED);
    }

    // ========== REPORTING QUERIES (Infrastructure Layer) ==========

    /**
     * Findet Reiseanträge in einem Zeitraum (nach Reisedatum)
     * Query über JPQL: Vergleicht embedded DateRange.startDate und endDate
     */
    @Override
    @Query("SELECT tr FROM TravelRequest tr " +
           "WHERE tr.travelPeriod.startDate >= :startDate " +
           "AND tr.travelPeriod.endDate <= :endDate " +
           "ORDER BY tr.createdAt DESC")
    List<TravelRequest> findByDateRange(@Param("startDate") LocalDate startDate, 
                                        @Param("endDate") LocalDate endDate);

    /**
     * Findet Reiseanträge nach Erstellungsdatum (für HR-Reporting)
     * Wichtig: Nutzt createdAt statt travelPeriod für Statistiken!
     * H2-kompatibel: CAST zu DATE statt DATE() Funktion
     */
    @Override
    @Query("SELECT tr FROM TravelRequest tr " +
           "WHERE CAST(tr.createdAt AS date) >= :startDate " +
           "AND CAST(tr.createdAt AS date) <= :endDate " +
           "ORDER BY tr.createdAt DESC")
    List<TravelRequest> findByCreatedAtRange(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    /**
     * Findet Reiseanträge mit bestimmtem Status in einem Zeitraum
     */
    @Override
    @Query("SELECT tr FROM TravelRequest tr " +
           "WHERE tr.status = :status " +
           "AND tr.travelPeriod.startDate >= :startDate " +
           "AND tr.travelPeriod.endDate <= :endDate " +
           "ORDER BY tr.createdAt DESC")
    List<TravelRequest> findByStatusAndDateRange(
        @Param("status") TravelRequestStatus status,
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate
    );

    /**
     * Zählt Reiseanträge nach Status in einem Zeitraum
     */
    @Override
    @Query("SELECT COUNT(tr) FROM TravelRequest tr " +
           "WHERE tr.status = :status " +
           "AND tr.travelPeriod.startDate >= :startDate " +
           "AND tr.travelPeriod.endDate <= :endDate")
    long countByStatusAndDateRange(
        @Param("status") TravelRequestStatus status,
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate
    );

    /**
     * Findet Reiseanträge nach Mitarbeiter-IDs (für Kostenstellen-Reporting)
     */
    @Override
    @Query("SELECT tr FROM TravelRequest tr WHERE tr.employeeId IN :employeeIds")
    List<TravelRequest> findByEmployeeIdIn(@Param("employeeIds") List<Long> employeeIds);

    /**
     * Findet Reiseanträge nach Kostenstellen-Code
     */
    @Override
    @Query("SELECT tr FROM TravelRequest tr WHERE tr.costCenter.code = :costCenterCode")
    List<TravelRequest> findByCostCenterCode(@Param("costCenterCode") String costCenterCode);

    /**
     * Findet Reiseanträge nach Kostenstellen-Code in einem Zeitraum
     */
    @Override
    @Query("SELECT tr FROM TravelRequest tr " +
           "WHERE tr.costCenter.code = :costCenterCode " +
           "AND tr.travelPeriod.startDate >= :startDate " +
           "AND tr.travelPeriod.endDate <= :endDate " +
           "ORDER BY tr.createdAt DESC")
    List<TravelRequest> findByCostCenterCodeAndDateRange(
        @Param("costCenterCode") String costCenterCode,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT tr FROM TravelRequest tr WHERE tr.status = 'PAID'")
    @Override
    List<TravelRequest> findAllReadyForArchiving();

    @Query("SELECT tr FROM TravelRequest tr " +
           "WHERE tr.status = 'ARCHIVED' " +
           "AND tr.retentionPeriod.retentionEndDate < CURRENT_DATE")
    @Override
    List<TravelRequest> findAllWithExpiredRetention();

    @Query("SELECT tr FROM TravelRequest tr " +
           "WHERE tr.status = 'ARCHIVED' " +
           "AND tr.retentionPeriod.archivedAt BETWEEN :startDate AND :endDate")
    @Override
    List<TravelRequest> findArchivedBetween(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
