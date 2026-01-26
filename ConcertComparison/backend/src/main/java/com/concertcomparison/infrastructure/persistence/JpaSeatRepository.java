package com.concertcomparison.infrastructure.persistence;

import com.concertcomparison.domain.model.Seat;
import com.concertcomparison.domain.model.SeatStatus;
import com.concertcomparison.domain.repository.SeatRepository;
import com.concertcomparison.domain.repository.SeatAvailabilityAggregate;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA Repository Implementation für Seat Aggregate (Adapter in Hexagonal Architecture).
 * 
 * Implementiert die SeatRepository Port-Schnittstelle mit Spring Data JPA.
 * Nutzt JpaRepository für Standard-CRUD-Operationen und custom Queries für Performance-Optimierung.
 * 
 * Performance-Optimierungen:
 * - Index auf concert_id für schnelle Lookups
 * - Index auf (concert_id, status) für Verfügbarkeitsabfragen
 * - DB-Aggregation für Kategorie-Zählung (statt In-Memory)
 * - Batch-Operations für Bulk-Inserts
 */
@Repository
public interface JpaSeatRepository extends JpaRepository<Seat, Long>, SeatRepository {
    
    /**
     * {@inheritDoc}
     * 
     * Implementiert via Spring Data saveAll(), nutzt JPA Batch Processing.
     */
    @Override
    default List<Seat> saveAllBatch(List<Seat> seats) {
        return saveAll(seats);
    }
    
    /**
     * {@inheritDoc}
     * 
     * Nutzt Index idx_concert_id für Performance.
     */
    @Override
    List<Seat> findByConcertId(Long concertId);
    
    /**
     * {@inheritDoc}
     * 
     * Nutzt Index idx_concert_status für Performance.
     */
    @Override
    List<Seat> findByConcertIdAndStatus(Long concertId, SeatStatus status);
    
    /**
     * {@inheritDoc}
     * 
     * Custom Query für abgelaufene Holds mit Index auf hold_expires_at.
     */
    @Query("SELECT s FROM Seat s WHERE s.status = 'HELD' AND s.holdExpiresAt < :now")
    @Override
    List<Seat> findExpiredHolds(@Param("now") LocalDateTime now);
    
    /**
     * {@inheritDoc}
     * 
     * PESSIMISTIC WRITE LOCK für High-Traffic Szenarien.
     * 
     * DB-Query: SELECT * FROM seats WHERE id = ? FOR UPDATE
     * 
     * Verhindert parallele Zugriffe während der Transaktion.
     * Andere Threads warten bis Lock freigegeben wird.
     * 
     * Performance Trade-off:
     * - Pro: Keine Optimistic Lock Retries bei hoher Konfliktrate
     * - Contra: Serialisierung der Requests, niedrigerer Durchsatz
     * 
     * Empfohlen für: Ticket-Verkaufsstart mit 10.000+ req/s auf populäre Seats
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id = :id")
    @Override
    Optional<Seat> findByIdForUpdate(@Param("id") Long id);
    
    /**
     * {@inheritDoc}
     * 
     * DB-Aggregation via GROUP BY für Performance bei großen Seat-Anzahlen.
     * Liefert nur AVAILABLE Seats pro Kategorie.
     */
    @Query("SELECT s.category as category, COUNT(s) as count " +
           "FROM Seat s " +
           "WHERE s.concertId = :concertId AND s.status = 'AVAILABLE' " +
           "GROUP BY s.category")
    List<CategoryCountProjection> countAvailableSeatsPerCategoryNative(@Param("concertId") Long concertId);
    
    /**
     * {@inheritDoc}
     * 
     * Konvertiert DB-Projection zu Map für einfache Nutzung im Application Layer.
     */
    @Override
    default Map<String, Long> countAvailableSeatsPerCategory(Long concertId) {
        return countAvailableSeatsPerCategoryNative(concertId).stream()
            .collect(Collectors.toMap(
                CategoryCountProjection::getCategory,
                CategoryCountProjection::getCount
            ));
    }

    /**
     * Aggregiert Availability und Preisrange für mehrere Concert-IDs in einem Query.
     */
    @Query("SELECT s.concertId as concertId, " +
           "SUM(CASE WHEN s.status = 'AVAILABLE' THEN 1 ELSE 0 END) as availableSeats, " +
           "COUNT(s) as totalSeats, " +
           "MIN(s.price) as minPrice, " +
           "MAX(s.price) as maxPrice " +
           "FROM Seat s WHERE s.concertId IN :concertIds GROUP BY s.concertId")
    List<AvailabilityProjection> aggregateAvailabilityRaw(@Param("concertIds") List<Long> concertIds);

    @Override
    default Map<Long, SeatAvailabilityAggregate> aggregateAvailabilityByConcertIds(List<Long> concertIds) {
        if (concertIds == null || concertIds.isEmpty()) {
            return Map.of();
        }

        return aggregateAvailabilityRaw(concertIds).stream()
            .collect(Collectors.toMap(
                AvailabilityProjection::getConcertId,
                projection -> new SeatAvailabilityAggregate(
                    projection.getConcertId(),
                    projection.getAvailableSeats(),
                    projection.getTotalSeats(),
                    projection.getMinPrice(),
                    projection.getMaxPrice()
                )
            ));
    }
    
    /**
     * {@inheritDoc}
     * 
     * Spring Data JPA Standard-Methode.
     */
    @Override
    long countByConcertId(Long concertId);
    
    /**
     * {@inheritDoc}
     * 
     * Löscht alle Seats für ein bestimmtes Konzert.
     * Nutzt Bulk DELETE für Performance.
     */
    @Modifying
    @Query("DELETE FROM Seat s WHERE s.concertId = :concertId")
    @Override
    int deleteAllByConcertId(@Param("concertId") Long concertId);
    
    /**
     * Projection Interface für DB-Aggregation Query.
     * 
     * Wird von Spring Data JPA automatisch gemappt.
     */
    interface CategoryCountProjection {
        String getCategory();
        Long getCount();
    }

    /**
     * Projection für Availability-Aggregation.
     */
    interface AvailabilityProjection {
        Long getConcertId();
        Long getAvailableSeats();
        Long getTotalSeats();
        Double getMinPrice();
        Double getMaxPrice();
    }
}
