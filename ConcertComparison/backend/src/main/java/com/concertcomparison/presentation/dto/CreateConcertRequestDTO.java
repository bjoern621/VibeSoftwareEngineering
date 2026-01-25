package com.concertcomparison.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Request DTO für Concert-Erstellung (POST /api/concerts).
 * 
 * Validierung:
 * - name: Nicht leer, maximal 200 Zeichen
 * - date: Muss in der Zukunft liegen
 * - venue: Nicht leer, maximal 500 Zeichen
 * - description: Optional, maximal 2000 Zeichen
 */
@Schema(description = "Request zum Erstellen eines neuen Konzerts")
public class CreateConcertRequestDTO {
    
    @NotBlank(message = "Concert name is required")
    @Schema(
        description = "Name des Konzerts",
        example = "Metallica Live 2025",
        maxLength = 200
    )
    private String name;
    
    @NotNull(message = "Event date is required")
    @Future(message = "Event date must be in the future")
    @Schema(
        description = "Datum und Zeit des Konzerts (ISO 8601)",
        example = "2025-06-15T20:00:00",
        pattern = "yyyy-MM-dd'T'HH:mm:ss"
    )
    private LocalDateTime date;
    
    @NotBlank(message = "Venue is required")
    @Schema(
        description = "Veranstaltungsort",
        example = "Mercedes-Benz Arena Berlin",
        maxLength = 500
    )
    private String venue;
    
    @Schema(
        description = "Optionale Beschreibung des Konzerts",
        example = "Exklusives Metallica-Konzert mit Vollständige Album-Performance",
        maxLength = 2000,
        nullable = true
    )
    private String description;
    
    // ==================== CONSTRUCTORS ====================
    
    public CreateConcertRequestDTO() {}
    
    public CreateConcertRequestDTO(String name, LocalDateTime date, String venue, String description) {
        this.name = name;
        this.date = date;
        this.venue = venue;
        this.description = description;
    }
    
    // ==================== GETTERS & SETTERS ====================
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public LocalDateTime getDate() {
        return date;
    }
    
    public void setDate(LocalDateTime date) {
        this.date = date;
    }
    
    public String getVenue() {
        return venue;
    }
    
    public void setVenue(String venue) {
        this.venue = venue;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}
