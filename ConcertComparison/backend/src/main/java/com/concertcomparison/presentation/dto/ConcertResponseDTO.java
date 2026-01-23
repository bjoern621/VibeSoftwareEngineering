package com.concertcomparison.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Response DTO für Concert Entity.
 * 
 * Wird verwendet in REST Responses für Concert-Operationen.
 * Exponiert keine technischen Felder wie `version` (Optimistic Locking).
 * 
 * Builder Pattern für saubere Object-Erstellung.
 */
@Schema(description = "Response DTO für ein Konzert")
public class ConcertResponseDTO {
    
    @Schema(
        description = "Eindeutige Concert-ID",
        example = "1"
    )
    private String id;
    
    @Schema(
        description = "Name des Konzerts",
        example = "Metallica Live 2025"
    )
    private String name;
    
    @Schema(
        description = "Datum und Zeit des Konzerts (ISO 8601)",
        example = "2025-06-15T20:00:00"
    )
    private LocalDateTime date;
    
    @Schema(
        description = "Veranstaltungsort",
        example = "Mercedes-Benz Arena Berlin"
    )
    private String venue;
    
    @Schema(
        description = "Optionale Beschreibung",
        example = "Exklusives Metallica-Konzert",
        nullable = true
    )
    private String description;
    
    @Schema(
        description = "Erstellungszeitpunkt (ISO 8601)",
        example = "2025-01-23T10:30:00"
    )
    private LocalDateTime createdAt;
    
    @Schema(
        description = "Letzter Aktualisierungszeitpunkt (ISO 8601)",
        example = "2025-01-23T11:45:00"
    )
    private LocalDateTime updatedAt;
    
    // ==================== CONSTRUCTORS ====================
    
    public ConcertResponseDTO() {}
    
    private ConcertResponseDTO(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.date = builder.date;
        this.venue = builder.venue;
        this.description = builder.description;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }
    
    // ==================== BUILDER PATTERN ====================
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String id;
        private String name;
        private LocalDateTime date;
        private String venue;
        private String description;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        public Builder id(String id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder date(LocalDateTime date) { this.date = date; return this; }
        public Builder venue(String venue) { this.venue = venue; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        
        public ConcertResponseDTO build() {
            return new ConcertResponseDTO(this);
        }
    }
    
    // ==================== GETTERS ====================
    
    public String getId() { return id; }
    public String getName() { return name; }
    public LocalDateTime getDate() { return date; }
    public String getVenue() { return venue; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
