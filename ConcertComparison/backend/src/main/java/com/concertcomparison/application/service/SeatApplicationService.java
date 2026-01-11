package com.concertcomparison.application.service;

import com.concertcomparison.domain.model.Seat;
import com.concertcomparison.domain.model.SeatStatus;
import com.concertcomparison.domain.repository.SeatRepository;
import com.concertcomparison.presentation.dto.AvailabilityByCategoryDTO;
import com.concertcomparison.presentation.dto.SeatAvailabilityResponseDTO;
import com.concertcomparison.presentation.dto.SeatResponseDTO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Application Service für Seat-Verfügbarkeit Use Cases.
 * 
 * Orchestriert Domain Logic und DTO-Mapping.
 * Implementiert Caching für Performance-Optimierung.
 */
@Service
@Transactional(readOnly = true)
public class SeatApplicationService {
    
    private final SeatRepository seatRepository;
    
    public SeatApplicationService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }
    
    /**
     * Use Case: Alle Seats für ein Event abrufen mit Verfügbarkeit.
     * 
     * Cached für 5 Minuten (definiert in application.properties).
     * Cache-Invalidierung erfolgt bei Seat-Status-Änderungen.
     * 
     * @param eventId ID des Events (Concert)
     * @return DTO mit Seats und aggregierter Verfügbarkeit
     */
    @Cacheable(value = "seatAvailability", key = "#eventId")
    public SeatAvailabilityResponseDTO getSeatAvailability(Long eventId) {
        List<Seat> allSeats = seatRepository.findByConcertId(eventId);
        
        if (allSeats.isEmpty()) {
            return SeatAvailabilityResponseDTO.empty(String.valueOf(eventId));
        }
        
        // Seats zu DTOs mappen
        List<SeatResponseDTO> seatDTOs = allSeats.stream()
            .map(this::mapToSeatDTO)
            .collect(Collectors.toList());
        
        // Aggregierte Verfügbarkeit berechnen
        List<AvailabilityByCategoryDTO> categoryAvailability = 
            calculateCategoryAvailability(allSeats);
        
        return SeatAvailabilityResponseDTO.builder()
            .concertId(String.valueOf(eventId))  // Long -> String konvertieren
            .seats(seatDTOs)
            .availabilityByCategory(categoryAvailability)
            .build();
    }
    
    /**
     * Berechnet aggregierte Verfügbarkeit pro Kategorie.
     * 
     * Gruppiert Seats nach Kategorie und zählt Stati.
     * Gibt Liste (Array) zurück gemäß OpenAPI-Spec.
     * 
     * @param seats Liste aller Seats
     * @return Liste von AvailabilityByCategoryDTO
     */
    private List<AvailabilityByCategoryDTO> calculateCategoryAvailability(List<Seat> seats) {
        Map<String, List<Seat>> seatsByCategory = seats.stream()
            .collect(Collectors.groupingBy(Seat::getCategory));
        
        return seatsByCategory.entrySet().stream()
            .map(entry -> {
                String category = entry.getKey();
                List<Seat> categorySeats = entry.getValue();
                
                int available = (int) categorySeats.stream()
                    .filter(s -> s.getStatus() == SeatStatus.AVAILABLE)
                    .count();
                
                int held = (int) categorySeats.stream()
                    .filter(s -> s.getStatus() == SeatStatus.HELD)
                    .count();
                
                int sold = (int) categorySeats.stream()
                    .filter(s -> s.getStatus() == SeatStatus.SOLD)
                    .count();
                
                return new AvailabilityByCategoryDTO(category, available, held, sold);
            })
            .sorted((a, b) -> a.getCategory().compareTo(b.getCategory()))  // Alphabetisch sortieren
            .collect(Collectors.toList());
    }
    
    /**
     * Mappt Domain Entity zu Response DTO.
     * 
     * Konvertiert Long IDs zu String (OpenAPI-Konformität).
     * Exponiert nur API-relevante Felder.
     * 
     * @param seat Seat Entity
     * @return SeatResponseDTO
     */
    private SeatResponseDTO mapToSeatDTO(Seat seat) {
        return SeatResponseDTO.builder()
            .id(String.valueOf(seat.getId()))  // Long -> String
            .block(seat.getBlock())
            .category(seat.getCategory())
            .row(seat.getRow())
            .number(seat.getNumber())
            .price(seat.getPrice())
            .status(seat.getStatus().name())  // Enum -> String (z.B. "AVAILABLE")
            .build();
    }
}
