package com.concertcomparison.presentation.controller;

import com.concertcomparison.application.service.HoldApplicationService;
import com.concertcomparison.presentation.dto.HoldCreateRequestDTO;
import com.concertcomparison.presentation.dto.HoldResponseDTO;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller für Reservation/Hold-Operationen (US-02).
 * OpenAPI Endpoints:
 * - POST /seats/{id}/hold
 * - GET /reservations/{id}
 * - DELETE /reservations/{id}
 */
@RestController
@RequestMapping("/api")
public class ReservationController {

    private static final Logger logger = LoggerFactory.getLogger(ReservationController.class);

    private final HoldApplicationService holdApplicationService;

    public ReservationController(HoldApplicationService holdApplicationService) {
        this.holdApplicationService = holdApplicationService;
    }

    /**
     * POST /api/seats/{id}/hold
     * Erstellt einen Hold für einen Seat.
     * 
     * @param id Seat ID
     * @param request HoldCreateRequestDTO mit userId
     * @return 200 OK mit HoldResponseDTO
     *         409 CONFLICT wenn Seat nicht verfügbar oder Concurrency Conflict
     *         404 NOT FOUND wenn Seat nicht existiert
     */
    @PostMapping("/seats/{id}/hold")
    public ResponseEntity<?> createHold(
            @PathVariable String id,
            @Valid @RequestBody HoldCreateRequestDTO request) {
        
        logger.info("POST /api/seats/{}/hold - Creating hold for userId={}", id, request.userId());

        try {
            Long seatId = Long.parseLong(id);
            HoldResponseDTO response = holdApplicationService.createHold(seatId, request.userId());
            
            logger.info("Hold created: holdId={}, seatId={}", response.holdId(), response.seatId());
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Seat not found: seatId={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("SEAT_NOT_FOUND", e.getMessage()));
                
        } catch (IllegalStateException e) {
            logger.warn("Seat not available: seatId={}, reason={}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("SEAT_NOT_AVAILABLE", e.getMessage()));
                
        } catch (jakarta.persistence.OptimisticLockException e) {
            logger.warn("Concurrency conflict for seatId={}", id);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("CONCURRENCY_CONFLICT", 
                    "Dieser Sitzplatz wurde gerade von jemand anderem reserviert."));
        }
    }

    /**
     * GET /api/reservations/{id}
     * Liefert Details zu einer Reservation.
     * 
     * @param id Hold/Reservation ID
     * @return 200 OK mit HoldResponseDTO
     *         404 NOT FOUND wenn Hold nicht existiert
     */
    @GetMapping("/reservations/{id}")
    public ResponseEntity<?> getReservation(@PathVariable String id) {
        logger.info("GET /api/reservations/{}", id);

        try {
            Long holdId = Long.parseLong(id);
            HoldResponseDTO response = holdApplicationService.getHold(holdId);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Hold not found: holdId={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("HOLD_NOT_FOUND", e.getMessage()));
        }
    }

    /**
     * DELETE /api/reservations/{id}
     * Storniert eine Reservation (Hold-Freigabe).
     * 
     * @param id Hold/Reservation ID
     * @return 204 NO CONTENT
     *         404 NOT FOUND wenn Hold nicht existiert
     *         409 CONFLICT wenn Hold nicht aktiv ist
     */
    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<?> cancelReservation(@PathVariable String id) {
        logger.info("DELETE /api/reservations/{}", id);

        try {
            Long holdId = Long.parseLong(id);
            holdApplicationService.releaseHold(holdId);
            
            logger.info("Hold cancelled: holdId={}", holdId);
            return ResponseEntity.noContent().build();
            
        } catch (IllegalArgumentException e) {
            logger.warn("Hold not found: holdId={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("HOLD_NOT_FOUND", e.getMessage()));
                
        } catch (IllegalStateException e) {
            logger.warn("Hold not active: holdId={}, reason={}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("HOLD_NOT_ACTIVE", e.getMessage()));
        }
    }

    // Inner class für Error Response (temporary - sollte später zentralisiert werden)
    private record ErrorResponse(String code, String message) {}
}
