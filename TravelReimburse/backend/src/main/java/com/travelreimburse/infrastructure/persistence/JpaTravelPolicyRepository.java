package com.travelreimburse.infrastructure.persistence;

import com.travelreimburse.domain.model.TravelPolicy;
import com.travelreimburse.domain.repository.TravelPolicyRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA Repository für TravelPolicy (Infrastructure Layer)
 * Extends beide: Domain-Interface und Spring Data JpaRepository
 * 
 * DDD: Implementierung im Infrastructure Layer
 */
@Repository
public interface JpaTravelPolicyRepository 
    extends TravelPolicyRepository, JpaRepository<TravelPolicy, Long> {
    
    /**
     * Spring Data generiert automatisch die Implementierung
     */
    @Query("SELECT p FROM TravelPolicy p WHERE p.active = true")
    List<TravelPolicy> findAllActive();
    
    @Query("SELECT p FROM TravelPolicy p WHERE p.departmentCode = :departmentCode AND p.active = true")
    List<TravelPolicy> findByDepartmentCode(String departmentCode);
    
    @Query("SELECT p FROM TravelPolicy p WHERE p.location = :location AND p.active = true")
    List<TravelPolicy> findByLocation(String location);
    
    @Query("SELECT p FROM TravelPolicy p WHERE p.active = true AND p.autoApprovalEnabled = true")
    List<TravelPolicy> findActiveWithAutoApproval();
}
