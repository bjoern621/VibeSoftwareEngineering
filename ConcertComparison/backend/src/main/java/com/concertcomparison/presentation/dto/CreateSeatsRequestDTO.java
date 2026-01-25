package com.concertcomparison.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request DTO für Bulk Sitzplatz-Erstellung (POST /api/concerts/{id}/seats).
 * 
 * Ermöglicht die effiziente Erstellung mehrerer Sitzplätze in einer Operation.
 * 
 * Validierung:
 * - seats: Mindestens 1 Sitzplatz erforderlich
 * - Jeder Sitz wird validiert über CreateSeatRequestDTO
 */
@Schema(description = "Request zum Erstellen mehrerer Sitzplätze für ein Konzert")
public class CreateSeatsRequestDTO {
    
    @NotEmpty(message = "At least one seat is required")
    @Valid
    @Schema(
        description = "Liste der zu erstellenden Sitzplätze"
    )
    private List<CreateSeatRequestDTO> seats;
    
    // ==================== CONSTRUCTORS ====================
    
    public CreateSeatsRequestDTO() {}
    
    public CreateSeatsRequestDTO(List<CreateSeatRequestDTO> seats) {
        this.seats = seats;
    }
    
    // ==================== GETTERS & SETTERS ====================
    
    public List<CreateSeatRequestDTO> getSeats() {
        return seats;
    }
    
    public void setSeats(List<CreateSeatRequestDTO> seats) {
        this.seats = seats;
    }
}
