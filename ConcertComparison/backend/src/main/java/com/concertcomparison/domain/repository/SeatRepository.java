package com.concertcomparison.domain.repository;

import com.concertcomparison.domain.model.Seat;
import com.concertcomparison.domain.model.SeatStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository Interface für Seat Aggregate (Port in Hexagonal Architecture).
 * 
 * Definiert die Abstraktionen für Seat-Persistierung ohne konkrete Implementierung.
 * Die tatsächliche Implementierung erfolgt im Infrastructure Layer (Adapter).
 * 
 * Design Principles:
 * - Repository arbeitet mit Aggregates (Entities), nicht DTOs
 * - Keine Business-Logik in Queries
 * - Performance-optimierte Methoden für große Datenmengen
 */
public interface SeatRepository {
    
    /**
     * Findet alle Seats für ein bestimmtes Konzert.
     * 
     * @param concertId ID des Konzerts
     * @return Liste aller Seats für das Konzert (leer wenn keine gefunden)
     */
    List<Seat> findByConcertId(Long concertId);
    
    /**
     * Findet alle Seats für ein Konzert mit einem bestimmten Status.
     * 
     * Performance-optimiert für Verfügbarkeitsabfragen.
     * 
     * @param concertId ID des Konzerts
     * @param status Gewünschter Seat-Status (AVAILABLE, HELD, SOLD)
     * @return Liste der Seats mit angegebenem Status
     */
    List<Seat> findByConcertIdAndStatus(Long concertId, SeatStatus status);
    
    /**
     * Findet einen einzelnen Seat anhand seiner ID.
     * 
     * @param id Seat-ID
     * @return Optional mit Seat, falls gefunden
     */
    Optional<Seat> findById(Long id);
    
    /**
     * Findet einen Seat mit Pessimistic Write Lock (FOR UPDATE).
     * 
     * ⚠️ HIGH-PERFORMANCE MODE für kritische Szenarien:
     * - Verwendung bei > 10.000 Requests/Sekunde auf denselben Seat
     * - Verhindert Optimistic Lock Retries bei hoher Konfliktrate
     * - Blockiert andere Transaktionen bis Lock freigegeben wird
     * 
     * Transaktionsgarantie: Keine parallelen Änderungen möglich.
     * DB-Query: SELECT ... FROM seats WHERE id = ? FOR UPDATE
     * 
     * WICHTIG: Nur in @Transactional Context verwenden!
     * 
     * @param id Seat-ID
     * @return Optional mit Seat (locked), falls gefunden
     */
    Optional<Seat> findByIdForUpdate(Long id);
    
    /**
     * Findet alle Seats mit abgelaufenen Holds für automatische Bereinigung.
     * 
     * Wird vom Scheduler verwendet, um abgelaufene Reservierungen freizugeben.
     * Query: status = HELD AND holdExpiresAt < now
     * 
     * @param now Aktueller Zeitpunkt
     * @return Liste der Seats mit abgelaufenen Holds
     */
    List<Seat> findExpiredHolds(LocalDateTime now);
    
    /**
     * Speichert einen Seat (Create oder Update).
     * 
     * Geerbt von JpaRepository (Teil der Spring Data Standard-Methoden).
     * 
     * @param seat Zu speichernder Seat
     * @return Gespeicherter Seat (mit generierter ID bei Create)
     */
    Seat save(Seat seat);
    
    /**
     * Speichert mehrere Seats in einer Batch-Operation (Create oder Update).
     * 
     * Performance-optimiert mit JDBC Batch Processing (wenn konfiguriert):
     * spring.jpa.properties.hibernate.jdbc.batch_size=20
     * 
     * Nutzt eine Batch-Operation statt einzelner save() Calls für bessere Performance.
     * Diese Port-Methode ist absichtlich anders benannt, um Überschneidungen
     * mit Spring Data Basis-Methoden zu vermeiden.
     * 
     * @param seats Zu speichernde Seats
     * @return Liste der gespeicherten Seats
     */
    List<Seat> saveAllBatch(List<Seat> seats);
    
    /**
     * Zählt verfügbare Seats pro Kategorie für ein Konzert.
     * 
     * Performance-optimiert mit DB-aggregation (GROUP BY).
     * Verwendet für Availability-Aggregation ohne alle Seats laden zu müssen.
     * 
     * @param concertId ID des Konzerts
     * @return Map: Kategorie → Anzahl verfügbarer Seats
     */
    Map<String, Long> countAvailableSeatsPerCategory(Long concertId);

    /**
     * Aggregiert Verfügbarkeit, Gesamtanzahl und Preisrange für mehrere Concerts in einem Query.
     *
     * @param concertIds Liste der Concert-IDs
     * @return Map Concert-ID -> Aggregatwerte
     */
    Map<Long, SeatAvailabilityAggregate> aggregateAvailabilityByConcertIds(List<Long> concertIds);
    
    /**
     * Löscht einen Seat (nur für Admin/Testing).
     * 
     * @param seat Zu löschender Seat
     */
    void delete(Seat seat);
    
    /**
     * Prüft, ob ein Seat mit der gegebenen ID existiert.
     * 
     * @param id Seat-ID
     * @return true wenn Seat existiert
     */
    boolean existsById(Long id);
    
    /**
     * Zählt alle Seats für ein Konzert.
     * 
     * @param concertId ID des Konzerts
     * @return Anzahl der Seats
     */
    long countByConcertId(Long concertId);
    
    /**
     * Löscht alle Seats (nur für Tests).
     */
    void deleteAll();
    
    /**
     * Findet alle Seats (nur für Tests).
     * 
     * @return Liste aller Seats
     */
    List<Seat> findAll();
}
