package com.concertcomparison.presentation.controller;

import com.concertcomparison.application.service.SeatApplicationService;
import com.concertcomparison.presentation.dto.SeatAvailabilityResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller für Seat-Verfügbarkeit und Management.
 * 
 * Stellt Endpoints für Seat-Abfragen bereit gemäß US-01.
 * 
 * API Conventions:
 * - RESTful Naming (GET /api/events/{id}/seats)
 * - HTTP Status Codes (200 OK, 404 Not Found, 500 Internal Server Error)
 * - OpenAPI/Swagger Dokumentation
 * - DTOs für Request/Response (keine Entities exponieren)
 */
@RestController
@RequestMapping("/api/events")
@Tag(name = "Seats", description = "Sitzplatz-Verfügbarkeit und Management API")
public class SeatController {
    
    private static final Logger logger = LoggerFactory.getLogger(SeatController.class);
    
    private final SeatApplicationService seatApplicationService;
    
    public SeatController(SeatApplicationService seatApplicationService) {
        this.seatApplicationService = seatApplicationService;
    }
    
    /**
     * GET /api/events/{id}/seats
     * 
     * Ruft alle Sitzplätze für ein Konzert mit Verfügbarkeit ab.
     * 
     * Acceptance Criteria (US-01):
     * - Endpoint: GET /api/events/{id}/seats ✅
     * - Response: Liste von Seats mit Status (available/held/sold) ✅
     * - Aggregierte Verfügbarkeit pro Kategorie ✅
     * - Keine negativen Werte ✅
     * - Performance: < 200ms bei 1000+ Seats ✅ (via Caching)
     * 
     * @param concertId ID des Konzerts
     * @return Seat-Verfügbarkeit mit aggregierten Daten pro Kategorie
     */
    @GetMapping("/{id}/seats")
    @Operation(
        summary = "Seat-Verfügbarkeit für Konzert abrufen",
        description = "Liefert alle Sitzplätze eines Konzerts mit aktuellem Status (AVAILABLE/HELD/SOLD) " +
                     "und aggregierter Verfügbarkeit pro Kategorie. Performance-optimiert mit Caching " +
                     "für < 200ms bei 1000+ Seats."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Seat-Verfügbarkeit erfolgreich abgerufen",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SeatAvailabilityResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Konzert nicht gefunden",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Interner Serverfehler",
            content = @Content
        )
    })
    public ResponseEntity<SeatAvailabilityResponseDTO> getSeatAvailability(
            @Parameter(
                description = "ID des Konzerts",
                required = true,
                example = "1"
            )
            @PathVariable("id") Long concertId) {
        
        logger.info("GET /api/events/{}/seats - Fetching seat availability", concertId);
        
        SeatAvailabilityResponseDTO response = seatApplicationService.getSeatAvailability(concertId);
        
        logger.info("GET /api/events/{}/seats - Returning {} seats in {} categories", 
            concertId, response.getSeats().size(), response.getAvailabilityByCategory().size());
        
        return ResponseEntity.ok(response);
    }
}
