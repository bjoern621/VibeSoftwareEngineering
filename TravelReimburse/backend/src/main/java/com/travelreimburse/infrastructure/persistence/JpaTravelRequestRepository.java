package com.travelreimburse.infrastructure.persistence;

import com.travelreimburse.domain.model.TravelRequest;
import com.travelreimburse.domain.repository.TravelRequestRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA Repository Implementierung für TravelRequest
 * Kombiniert Spring Data JPA mit dem Domain Repository Interface
 * 
 * Spring Data JPA generiert automatisch die Implementierung für:
 * - save(), findById(), findAll(), delete(), etc. (von JpaRepository)
 * - findByEmployeeId() (durch Methodennamen-Parsing)
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
    List<TravelRequest> findByStatus(com.travelreimburse.domain.model.TravelRequestStatus status);

    /**
     * Findet alle eingereichten Anträge (Implementierung für Domain-Interface)
     */
    @Override
    default List<TravelRequest> findPendingApprovals() {
        return findByStatus(com.travelreimburse.domain.model.TravelRequestStatus.SUBMITTED);
    }
}
