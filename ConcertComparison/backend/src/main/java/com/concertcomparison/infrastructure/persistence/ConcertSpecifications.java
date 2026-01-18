package com.concertcomparison.infrastructure.persistence;

import com.concertcomparison.domain.model.Concert;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications für dynamische Concert-Filterung.
 *
 * Infrastructure Layer: Implementiert dynamische Queries mit Criteria API.
 * Ermöglicht flexible Kombination von Filtern ohne N Query-Methoden.
 *
 * Pattern: Specification Pattern (DDD Tactical Pattern).
 */
public class ConcertSpecifications {

    /**
     * Erstellt eine Specification für Concert-Filterung.
     *
     * @param date Filter nach Datum (optional)
     * @param venue Filter nach Venue (case-insensitive, contains, optional)
     * @param minPrice Filter nach minimalem Preis (optional, wird aktuell nicht direkt in Concert gespeichert)
     * @param maxPrice Filter nach maximalem Preis (optional, wird aktuell nicht direkt in Concert gespeichert)
     * @return Specification die alle Filter kombiniert
     */
    public static Specification<Concert> withFilters(
            LocalDate date,
            String venue,
            Double minPrice,
            Double maxPrice) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Date Filter - vergleicht nur das Datum (ohne Uhrzeit)
            if (date != null) {
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.atTime(23, 59, 59);
                predicates.add(criteriaBuilder.between(root.get("date"), startOfDay, endOfDay));
            }

            // Venue Filter - case-insensitive LIKE
            if (venue != null && !venue.isBlank()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("venue")),
                    "%" + venue.toLowerCase() + "%"
                ));
            }

            // Price Filter - HINWEIS: Concert Entity hat kein price Feld
            // Preis-Filter müssten über JOIN mit Seats implementiert werden
            // Für Performance wird dies im Application Layer nachträglich gefiltert
            // (Alternative: Materialized View oder zusätzliches minPrice Feld in Concert)

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Filter für zukünftige Concerts (date > now).
     *
     * @return Specification für zukünftige Concerts
     */
    public static Specification<Concert> isFuture() {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.greaterThan(root.get("date"), LocalDateTime.now());
    }

    /**
     * Filter für vergangene Concerts (date < now).
     *
     * @return Specification für vergangene Concerts
     */
    public static Specification<Concert> isPast() {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.lessThan(root.get("date"), LocalDateTime.now());
    }
}
