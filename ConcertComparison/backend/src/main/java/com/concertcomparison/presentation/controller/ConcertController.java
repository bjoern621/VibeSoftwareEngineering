package com.concertcomparison.presentation.controller;

import com.concertcomparison.application.service.ConcertApplicationService;
import com.concertcomparison.presentation.dto.ConcertPageDTO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST Controller für Concert-Abfragen (US-07).
 *
 * Presentation Layer: Exponiert Concert-Liste API gemäß OpenAPI Specification.
 *
 * Endpoints:
 * - GET /events - Liste aller Concerts mit Filtern, Sortierung und Pagination
 *
 * Security: Public Endpoint (kein Authentication/Authorization erforderlich)
 */
@RestController
@RequestMapping("/events")
@Validated  // Aktiviert Bean Validation auf Method-Parametern
public class ConcertController {

    private final ConcertApplicationService concertService;

    public ConcertController(ConcertApplicationService concertService) {
        this.concertService = concertService;
    }

    /**
     * GET /events - Liste aller Concerts mit Filtern, Sortierung und Pagination (US-07).
     *
     * Query-Parameter:
     * - date: Filter nach Datum (YYYY-MM-DD)
     * - venue: Filter nach Venue (case-insensitive contains)
     * - minPrice: Filter nach minimalem Preis
     * - maxPrice: Filter nach maximalem Preis
     * - sortBy: Sortierfeld (date, name, price)
     * - sortOrder: Sortierrichtung (asc, desc)
     * - page: Seite (0-basiert)
     * - size: Seitengröße (1-200)
     *
     * Response: ConcertPageDTO mit paginierten Concerts
     *
     * Performance: ≤ 1 Sekunde für Verfügbarkeitsabfrage (NFR)
     *
     * @return Paginierte Concert-Liste mit HTTP 200 OK
     */
    @GetMapping
    public ResponseEntity<ConcertPageDTO> getAllConcerts(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,

            @RequestParam(required = false)
            String venue,

            @RequestParam(required = false)
            @Min(0)
            Double minPrice,

            @RequestParam(required = false)
            @Min(0)
            Double maxPrice,

            @RequestParam(defaultValue = "date")
            String sortBy,

            @RequestParam(defaultValue = "asc")
            String sortOrder,

            @RequestParam(defaultValue = "0")
            @Min(0)
            int page,

            @RequestParam(defaultValue = "20")
            @Min(1)
            @Max(200)
            int size) {

        ConcertPageDTO result = concertService.getAllConcerts(
            date,
            venue,
            minPrice,
            maxPrice,
            sortBy,
            sortOrder,
            page,
            size
        );

        return ResponseEntity.ok(result);
    }
}
