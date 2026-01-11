package com.concertcomparison.presentation.dto;

import java.util.List;
import java.util.Map;

/**
 * Response DTO für Seat-Verfügbarkeit eines Konzerts.
 * 
 * Aggregiert alle Seat-Informationen für ein Konzert:
 * - Einzelne Seats mit Status
 * - Aggregierte Verfügbarkeit pro Kategorie
 * - Gesamtstatistiken
 */
public class SeatAvailabilityResponseDTO {
    
    private Long concertId;
    private List<SeatResponseDTO> seats;
    private Map<String, CategoryAvailability> categoryAvailability;
    private long totalSeats;
    private long availableSeats;
    
    // ==================== CONSTRUCTORS ====================
    
    public SeatAvailabilityResponseDTO() {
        // Default Constructor für Jackson Deserialization
    }
    
    private SeatAvailabilityResponseDTO(Builder builder) {
        this.concertId = builder.concertId;
        this.seats = builder.seats;
        this.categoryAvailability = builder.categoryAvailability;
        this.totalSeats = builder.totalSeats;
        this.availableSeats = builder.availableSeats;
    }
    
    // ==================== BUILDER PATTERN ====================
    
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Erstellt eine leere Response für Konzerte ohne Seats.
     * 
     * @param concertId ID des Konzerts
     * @return Leere SeatAvailabilityResponseDTO
     */
    public static SeatAvailabilityResponseDTO empty(Long concertId) {
        return builder()
            .concertId(concertId)
            .seats(List.of())
            .categoryAvailability(Map.of())
            .totalSeats(0)
            .availableSeats(0)
            .build();
    }
    
    public static class Builder {
        private Long concertId;
        private List<SeatResponseDTO> seats;
        private Map<String, CategoryAvailability> categoryAvailability;
        private long totalSeats;
        private long availableSeats;
        
        public Builder concertId(Long concertId) {
            this.concertId = concertId;
            return this;
        }
        
        public Builder seats(List<SeatResponseDTO> seats) {
            this.seats = seats;
            return this;
        }
        
        public Builder categoryAvailability(Map<String, CategoryAvailability> categoryAvailability) {
            this.categoryAvailability = categoryAvailability;
            return this;
        }
        
        public Builder totalSeats(long totalSeats) {
            this.totalSeats = totalSeats;
            return this;
        }
        
        public Builder availableSeats(long availableSeats) {
            this.availableSeats = availableSeats;
            return this;
        }
        
        public SeatAvailabilityResponseDTO build() {
            return new SeatAvailabilityResponseDTO(this);
        }
    }
    
    // ==================== GETTERS & SETTERS ====================
    
    public Long getConcertId() {
        return concertId;
    }
    
    public void setConcertId(Long concertId) {
        this.concertId = concertId;
    }
    
    public List<SeatResponseDTO> getSeats() {
        return seats;
    }
    
    public void setSeats(List<SeatResponseDTO> seats) {
        this.seats = seats;
    }
    
    public Map<String, CategoryAvailability> getCategoryAvailability() {
        return categoryAvailability;
    }
    
    public void setCategoryAvailability(Map<String, CategoryAvailability> categoryAvailability) {
        this.categoryAvailability = categoryAvailability;
    }
    
    public long getTotalSeats() {
        return totalSeats;
    }
    
    public void setTotalSeats(long totalSeats) {
        this.totalSeats = totalSeats;
    }
    
    public long getAvailableSeats() {
        return availableSeats;
    }
    
    public void setAvailableSeats(long availableSeats) {
        this.availableSeats = availableSeats;
    }
}
