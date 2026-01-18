package com.concertcomparison.application.service;

import com.concertcomparison.domain.model.Concert;
import com.concertcomparison.domain.model.SeatStatus;
import com.concertcomparison.domain.repository.ConcertRepository;
import com.concertcomparison.domain.repository.SeatRepository;
import com.concertcomparison.infrastructure.persistence.ConcertSpecifications;
import com.concertcomparison.presentation.dto.ConcertListItemDTO;
import com.concertcomparison.presentation.dto.ConcertPageDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Application Service für Concert-Abfragen (US-07).
 *
 * Application Layer: Orchestriert Use Cases ohne Business-Logik.
 * Koordiniert zwischen Domain Repositories und Presentation DTOs.
 *
 * Verantwortlichkeiten:
 * - Filterung und Pagination von Concerts
 * - Aggregation von Availability-Daten aus Seats
 * - Mapping von Entities zu DTOs
 *
 * Performance-Optimierungen:
 * - Read-only Transaktionen
 * - Effiziente Availability-Checks via existsByConcertIdAndStatus
 * - MinPrice-Aggregation via DB-Query
 */
@Service
@Transactional(readOnly = true)
public class ConcertApplicationService {

    private final ConcertRepository concertRepository;
    private final SeatRepository seatRepository;

    public ConcertApplicationService(
            ConcertRepository concertRepository,
            SeatRepository seatRepository) {
        this.concertRepository = concertRepository;
        this.seatRepository = seatRepository;
    }

    /**
     * Liefert gefilterte, sortierte und paginierte Concert-Liste (US-07).
     *
     * @param date Filter nach Datum (optional)
     * @param venue Filter nach Venue (optional, case-insensitive)
     * @param minPrice Filter nach minimalem Preis (optional)
     * @param maxPrice Filter nach maximalem Preis (optional)
     * @param sortBy Sortierfeld (date, name, price)
     * @param sortOrder Sortierrichtung (asc, desc)
     * @param page Seite (0-basiert)
     * @param size Seitengröße
     * @return Paginierte Concert-Liste mit Availability
     */
    public ConcertPageDTO getAllConcerts(
            LocalDate date,
            String venue,
            Double minPrice,
            Double maxPrice,
            String sortBy,
            String sortOrder,
            int page,
            int size) {

        // 1. Build Specification für Filter
        Specification<Concert> spec = ConcertSpecifications.withFilters(date, venue, minPrice, maxPrice);

        // 2. Build Pageable für Sortierung und Pagination
        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder)
            ? Sort.Direction.DESC
            : Sort.Direction.ASC;

        String sortField = mapSortField(sortBy);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        // 3. Query Concerts
        Page<Concert> concertPage = concertRepository.findAll(spec, pageable);

        // 4. Map zu DTOs mit Availability und MinPrice
        List<ConcertListItemDTO> items = concertPage.getContent().stream()
            .map(this::mapToListItemDTO)
            .filter(dto -> filterByPrice(dto, minPrice, maxPrice)) // Post-filter für Preis
            .toList();

        // 5. Build Response
        return new ConcertPageDTO(
            page,
            size,
            concertPage.getTotalElements(),
            items
        );
    }

    /**
     * Mappt Concert Entity zu DTO mit Availability und MinPrice.
     *
     * Performance: 2 DB-Queries pro Concert
     * - existsByConcertIdAndStatus: Stoppt bei erstem AVAILABLE Seat
     * - findMinPriceByConcertId: DB-Aggregation MIN()
     *
     * @param concert Concert Entity
     * @return DTO mit Availability
     */
    private ConcertListItemDTO mapToListItemDTO(Concert concert) {
        // Availability Check: Mindestens ein AVAILABLE Seat?
        boolean available = seatRepository.existsByConcertIdAndStatus(
            concert.getId(),
            SeatStatus.AVAILABLE
        );

        // MinPrice via DB-Aggregation
        Double minPrice = seatRepository.findMinPriceByConcertId(concert.getId());

        return new ConcertListItemDTO(
            concert.getId().toString(),
            concert.getName(),
            concert.getDate().toLocalDate(),
            concert.getVenue(),
            minPrice,
            available
        );
    }

    /**
     * Post-Filter für Preis (wird nach DB-Query angewendet).
     *
     * HINWEIS: Preis-Filter können nicht in DB-Query integriert werden,
     * da Concert Entity kein minPrice Feld hat (nur Seats haben Preise).
     *
     * Alternative Lösungen:
     * - Denormalisierung: minPrice in Concert speichern (Update bei Seat-Änderungen)
     * - Materialized View: DB-View mit aggregiertem minPrice
     * - Aktuelle Lösung: Post-Filter im Application Layer (einfach, aber weniger effizient)
     *
     * @param dto Concert DTO
     * @param minPrice Minimaler Preis (optional)
     * @param maxPrice Maximaler Preis (optional)
     * @return true wenn DTO den Preis-Filter erfüllt
     */
    private boolean filterByPrice(ConcertListItemDTO dto, Double minPrice, Double maxPrice) {
        if (dto.minPrice() == null) {
            return true; // Kein Preis vorhanden (keine Seats) → durchlassen
        }

        if (minPrice != null && dto.minPrice() < minPrice) {
            return false;
        }

        if (maxPrice != null && dto.minPrice() > maxPrice) {
            return false;
        }

        return true;
    }

    /**
     * Mappt sortBy Parameter zu Concert Entity Feld.
     *
     * @param sortBy sortBy Parameter (date, name, price)
     * @return Entity Feldname
     */
    private String mapSortField(String sortBy) {
        if (sortBy == null) {
            return "date";
        }

        return switch (sortBy.toLowerCase()) {
            case "name" -> "name";
            case "price" -> "date"; // Preis-Sortierung nicht möglich (kein Feld in Concert)
            default -> "date";
        };
    }
}
