package com.travelreimburse.infrastructure.persistence;

import com.travelreimburse.domain.model.TravelDestination;
import com.travelreimburse.domain.repository.TravelDestinationRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository Implementierung für TravelDestination
 * Kombiniert Spring Data JPA mit dem Domain Repository Interface
 * 
 * Spring Data JPA generiert automatisch die Implementierung für:
 * - save(), findById(), findAll(), delete(), etc. (von JpaRepository)
 * - Custom Queries durch Methodennamen-Parsing oder @Query
 * 
 * DDD: Implementierung im Infrastructure Layer
 */
@Repository
public interface JpaTravelDestinationRepository 
    extends JpaRepository<TravelDestination, Long>, TravelDestinationRepository {
    
    /**
     * Spring Data JPA generiert automatisch die Query:
     * SELECT * FROM travel_destinations WHERE country_code = ?
     */
    @Override
    @Query("SELECT td FROM TravelDestination td WHERE td.countryCode.code = :countryCode")
    Optional<TravelDestination> findByCountryCode(@Param("countryCode") String countryCode);
    
    /**
     * Spring Data JPA generiert automatisch die Query basierend auf Embeddable-Feld
     */
    @Override
    @Query("SELECT td FROM TravelDestination td WHERE td.visaRequirement.required = :required")
    List<TravelDestination> findByVisaRequired(@Param("required") boolean required);
    
    /**
     * Spring Data JPA generiert automatisch die Query basierend auf Embeddable-Feld
     */
    @Override
    @Query("SELECT td FROM TravelDestination td WHERE td.vaccinationRequirement.required = :required")
    List<TravelDestination> findByVaccinationRequired(@Param("required") boolean required);
    
    /**
     * Spring Data JPA generiert automatisch die Query:
     * SELECT COUNT(*) > 0 FROM travel_destinations WHERE country_code = ?
     */
    @Override
    @Query("SELECT CASE WHEN COUNT(td) > 0 THEN true ELSE false END FROM TravelDestination td WHERE td.countryCode.code = :countryCode")
    boolean existsByCountryCode(@Param("countryCode") String countryCode);
}
