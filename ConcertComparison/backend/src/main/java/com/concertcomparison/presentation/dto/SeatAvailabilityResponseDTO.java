package com.concertcomparison.presentation.dto;

import java.util.List;

/**
 * Response DTO für Seat-Verfügbarkeit eines Konzerts.
 * Entspricht OpenAPI-Schema "SeatAvailabilityResponse".
 * 
 * Liefert vollständige Seat-Liste plus aggregierte Verfügbarkeit pro Kategorie.
 */
public class SeatAvailabilityResponseDTO {
    
    private String concertId;  // String gemäß OpenAPI
    private List<SeatResponseDTO> seats;
    private List<AvailabilityByCategoryDTO> availabilityByCategory;  // Array gemäß OpenAPI
    
    // Constructors
    public SeatAvailabilityResponseDTO() {}
    
    private SeatAvailabilityResponseDTO(Builder builder) {
        this.concertId = builder.concertId;
        this.seats = builder.seats;
        this.availabilityByCategory = builder.availabilityByCategory;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static SeatAvailabilityResponseDTO empty(String concertId) {
        return builder()
            .concertId(concertId)
            .seats(List.of())
            .availabilityByCategory(List.of())
            .build();
    }
    
    public static class Builder {
        private String concertId;
        private List<SeatResponseDTO> seats;
        private List<AvailabilityByCategoryDTO> availabilityByCategory;
        
        public Builder concertId(String concertId) {
            this.concertId = concertId;
            return this;
        }
        
        public Builder seats(List<SeatResponseDTO> seats) {
            this.seats = seats;
            return this;
        }
        
        public Builder availabilityByCategory(List<AvailabilityByCategoryDTO> availabilityByCategory) {
            this.availabilityByCategory = availabilityByCategory;
            return this;
        }
        
        public SeatAvailabilityResponseDTO build() {
            return new SeatAvailabilityResponseDTO(this);
        }
    }
    
    // Getters
    public String getConcertId() {
        return concertId;
    }
    
    public List<SeatResponseDTO> getSeats() {
        return seats;
    }
    
    public List<AvailabilityByCategoryDTO> getAvailabilityByCategory() {
        return availabilityByCategory;
    }
}
