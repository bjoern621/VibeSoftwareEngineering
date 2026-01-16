package com.concertcomparison.domain.repository;

import com.concertcomparison.domain.model.Concert;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository Interface für Concert Aggregate Root.
 * 
 * DDD Port (Interface im Domain Layer, Implementierung in Infrastructure).
 * Definiert alle Datenbank-Operationen für Concerts.
 */
public interface ConcertRepository {
    
    /**
     * Speichert ein Concert (Create oder Update).
     * 
     * @param concert Zu speicherndes Concert
     * @return Gespeichertes Concert mit generierter ID
     */
    Concert save(Concert concert);
    
    /**
     * Sucht ein Concert anhand der ID.
     * 
     * @param id Concert-ID
     * @return Optional mit Concert, oder empty wenn nicht gefunden
     */
    Optional<Concert> findById(Long id);
    
    /**
     * Liefert alle Concerts.
     * 
     * @return Liste aller Concerts
     */
    List<Concert> findAll();
    
    /**
     * Sucht Concerts nach Name (Teilstring-Suche, case-insensitive).
     * 
     * @param name Name oder Teilstring
     * @return Liste der gefundenen Concerts
     */
    List<Concert> findByNameContainingIgnoreCase(String name);
    
    /**
     * Sucht Concerts nach Venue (Teilstring-Suche, case-insensitive).
     * 
     * @param venue Venue oder Teilstring
     * @return Liste der gefundenen Concerts
     */
    List<Concert> findByVenueContainingIgnoreCase(String venue);
    
    /**
     * Sucht Concerts in einem Zeitraum.
     * 
     * @param startDate Start-Datum (inklusiv)
     * @param endDate End-Datum (inklusiv)
     * @return Liste der Concerts im Zeitraum
     */
    List<Concert> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Sucht zukünftige Concerts (date > now).
     * 
     * @return Liste der zukünftigen Concerts, sortiert nach Datum aufsteigend
     */
    List<Concert> findFutureConcerts();
    
    /**
     * Sucht vergangene Concerts (date < now).
     * 
     * @return Liste der vergangenen Concerts, sortiert nach Datum absteigend
     */
    List<Concert> findPastConcerts();
    
    /**
     * Löscht ein Concert anhand der ID.
     * 
     * Business Rule: Nur Concerts ohne verkaufte Tickets sollten gelöscht werden.
     * 
     * @param id Concert-ID
     */
    void deleteById(Long id);
    
    /**
     * Prüft ob ein Concert mit der ID existiert.
     * 
     * @param id Concert-ID
     * @return true wenn Concert existiert
     */
    boolean existsById(Long id);
}
