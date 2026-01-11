package com.concertcomparison.application.service;

import com.concertcomparison.domain.model.Seat;
import com.concertcomparison.domain.model.SeatStatus;
import com.concertcomparison.domain.repository.SeatRepository;
import com.concertcomparison.presentation.dto.CategoryAvailability;
import com.concertcomparison.presentation.dto.SeatAvailabilityResponseDTO;
import com.concertcomparison.presentation.dto.SeatResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Application Service für Seat-Management.
 * 
 * Orchestriert Use Cases für Verfügbarkeitsabfragen und Seat-Operationen.
 * Koordiniert zwischen Domain Layer (Entities, Repositories) und Presentation Layer (DTOs).
 * 
 * Design Principles:
 * - Use-Case-fokussiert (1 Methode = 1 Use Case)
 * - Transaktionsgrenzen definieren (@Transactional)
 * - Domain Entities → DTOs mapping
 * - Keine Business-Logik (nur Orchestrierung)
 */
@Service
@Transactional(readOnly = true)
public class SeatApplicationService {
    
    private static final Logger logger = LoggerFactory.getLogger(SeatApplicationService.class);
    
    private final SeatRepository seatRepository;
    
    public SeatApplicationService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }
    
    /**
     * Use Case US-01: Seat-Verfügbarkeit für ein Konzert abrufen.
     * 
     * Liefert alle Seats eines Konzerts mit aktuellem Status und aggregierter Verfügbarkeit
     * pro Kategorie. Performance-optimiert mit Caching für < 200ms bei 1000+ Seats.
     * 
     * Acceptance Criteria:
     * - Response: Liste von Seats mit Status (available/held/sold)
     * - Aggregierte Verfügbarkeit pro Kategorie
     * - Keine negativen Werte
     * - Performance: < 200ms bei 1000+ Seats
     * 
     * @param concertId ID des Konzerts
     * @return DTO mit Seats und aggregierter Verfügbarkeit
     */
    @Cacheable(value = "seatAvailability", key = "#concertId", unless = "#result.totalSeats == 0")
    public SeatAvailabilityResponseDTO getSeatAvailability(Long concertId) {
        logger.debug("Fetching seat availability for concert {}", concertId);
        
        long startTime = System.currentTimeMillis();
        
        // 1. Alle Seats für Konzert laden
        List<Seat> allSeats = seatRepository.findByConcertId(concertId);
        
        if (allSeats.isEmpty()) {
            logger.info("No seats found for concert {}", concertId);
            return SeatAvailabilityResponseDTO.empty(concertId);
        }
        
        // 2. Seats zu DTOs mappen
        List<SeatResponseDTO> seatDTOs = allSeats.stream()
            .map(this::mapToSeatDTO)
            .collect(Collectors.toList());
        
        // 3. Aggregierte Verfügbarkeit berechnen
        Map<String, CategoryAvailability> categoryAvailability = calculateCategoryAvailability(allSeats);
        
        // 4. Gesamtstatistik berechnen
        long availableSeats = countSeatsByStatus(allSeats, SeatStatus.AVAILABLE);
        
        // 5. Response DTO erstellen
        SeatAvailabilityResponseDTO response = SeatAvailabilityResponseDTO.builder()
            .concertId(concertId)
            .seats(seatDTOs)
            .categoryAvailability(categoryAvailability)
            .totalSeats(allSeats.size())
            .availableSeats(availableSeats)
            .build();
        
        long duration = System.currentTimeMillis() - startTime;
        logger.debug("Fetched {} seats for concert {} in {}ms", allSeats.size(), concertId, duration);
        
        return response;
    }
    
    /**
     * Berechnet aggregierte Verfügbarkeit pro Kategorie.
     * 
     * Gruppiert alle Seats nach Kategorie und zählt Status-Verteilung.
     * Garantiert keine negativen Werte (Acceptance Criteria).
     * 
     * @param seats Liste aller Seats
     * @return Map: Kategorie → CategoryAvailability
     */
    private Map<String, CategoryAvailability> calculateCategoryAvailability(List<Seat> seats) {
        return seats.stream()
            .collect(Collectors.groupingBy(
                Seat::getCategory,
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    categorySeats -> {
                        long total = categorySeats.size();
                        long available = countSeatsByStatus(categorySeats, SeatStatus.AVAILABLE);
                        long held = countSeatsByStatus(categorySeats, SeatStatus.HELD);
                        long sold = countSeatsByStatus(categorySeats, SeatStatus.SOLD);
                        
                        // Validierung: Summe muss aufgehen
                        assert available + held + sold == total : 
                            "Inkonsistente Seat-Zählung für Kategorie " + categorySeats.get(0).getCategory();
                        
                        return new CategoryAvailability(
                            categorySeats.get(0).getCategory(),
                            total,
                            available,
                            held,
                            sold
                        );
                    }
                )
            ));
    }
    
    /**
     * Zählt Seats mit bestimmtem Status.
     * 
     * @param seats Liste der Seats
     * @param status Zu zählender Status
     * @return Anzahl Seats mit angegebenem Status (≥ 0)
     */
    private long countSeatsByStatus(List<Seat> seats, SeatStatus status) {
        return seats.stream()
            .filter(seat -> seat.getStatus() == status)
            .count();
    }
    
    /**
     * Mappt Domain Entity zu Response DTO.
     * 
     * Kapselt Mapping-Logik, verhindert Exponierung von Domain Entities via REST.
     * 
     * @param seat Seat Entity
     * @return SeatResponseDTO
     */
    private SeatResponseDTO mapToSeatDTO(Seat seat) {
        return SeatResponseDTO.builder()
            .id(seat.getId())
            .seatNumber(seat.getSeatNumber())
            .category(seat.getCategory())
            .block(seat.getBlock())
            .status(seat.getStatus().name())
            .statusDisplayName(seat.getStatus().getDisplayName())
            .isAvailable(seat.getStatus() == SeatStatus.AVAILABLE)
            .holdExpiresAt(seat.getHoldExpiresAt())
            .build();
    }
}
