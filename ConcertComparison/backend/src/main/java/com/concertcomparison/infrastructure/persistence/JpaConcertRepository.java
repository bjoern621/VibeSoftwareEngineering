package com.concertcomparison.infrastructure.persistence;

import com.concertcomparison.domain.model.Concert;
import com.concertcomparison.domain.repository.ConcertRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA Implementierung des ConcertRepository.
 * 
 * Infrastructure Layer: Implementiert das Domain Repository Interface.
 * Verwendet Spring Data JPA für automatische Query-Generierung.
 */
@Repository
public interface JpaConcertRepository extends JpaRepository<Concert, Long>, ConcertRepository {
    
    // Spring Data JPA generiert automatisch Queries basierend auf Methodennamen
    
    @Override
    List<Concert> findByNameContainingIgnoreCase(String name);
    
    @Override
    List<Concert> findByVenueContainingIgnoreCase(String venue);
    
    @Override
    List<Concert> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Custom Query für zukünftige Concerts.
     */
    @Query("SELECT c FROM Concert c WHERE c.date > CURRENT_TIMESTAMP ORDER BY c.date ASC")
    @Override
    List<Concert> findFutureConcerts();
    
    /**
     * Custom Query für vergangene Concerts.
     */
    @Query("SELECT c FROM Concert c WHERE c.date < CURRENT_TIMESTAMP ORDER BY c.date DESC")
    @Override
    List<Concert> findPastConcerts();
}
