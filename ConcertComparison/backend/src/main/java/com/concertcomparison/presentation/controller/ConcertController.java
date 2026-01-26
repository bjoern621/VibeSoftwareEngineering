package com.concertcomparison.presentation.controller;

import com.concertcomparison.application.service.ConcertApplicationService;
import com.concertcomparison.domain.repository.ConcertFilterCriteria;
import com.concertcomparison.presentation.dto.CreateConcertRequestDTO;
import com.concertcomparison.presentation.dto.CreateSeatsRequestDTO;
import com.concertcomparison.presentation.dto.ConcertResponseDTO;
import com.concertcomparison.presentation.dto.PagedConcertResponseDTO;
import com.concertcomparison.presentation.dto.UpdateConcertRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * REST Controller für Admin-Operationen auf Concerts und Seats.
 * 
 * Implementiert folgende US-09 Requirements:
 * - Endpoint: POST /api/concerts (CREATE Concert)
 * - Endpoint: PUT /api/concerts/{id} (UPDATE Concert)
 * - Endpoint: DELETE /api/concerts/{id} (DELETE Concert)
 * - Endpoint: POST /api/concerts/{id}/seats (BULK CREATE Seats)
 * 
 * Security:
 * - Alle Endpoints erfordern ADMIN-Rolle
 * - @PreAuthorize("hasRole('ADMIN')") auf Klasse/Methoden
 * 
 * API Conventions:
 * - RESTful Naming
 * - HTTP Status Codes (201 Created, 200 OK, 400 Bad Request, 409 Conflict, 403 Forbidden)
 * - OpenAPI/Swagger Dokumentation
 * - DTOs für Request/Response (keine Entities exponieren)
 * - Validation mit @Valid auf Request DTOs
 */
@RestController
@RequestMapping("/api/concerts")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Concerts (Admin)", description = "Admin-Endpoints für Concert- und Seat-Management (US-09)")
public class ConcertController {
    
    private static final Logger logger = LoggerFactory.getLogger(ConcertController.class);
    
    private final ConcertApplicationService concertApplicationService;
    
    public ConcertController(ConcertApplicationService concertApplicationService) {
        this.concertApplicationService = concertApplicationService;
    }
    
    /**
     * POST /api/concerts
     * 
     * Erstellt ein neues Concert.
     * 
     * Acceptance Criteria (US-09):
     * - Endpoint: POST /api/concerts ✅
     * - Request: CreateConcertRequestDTO mit Validierung ✅
     * - Response: ConcertResponseDTO ✅
     * - HTTP Status: 201 Created ✅
     * - Nur ADMIN-Rolle ✅
     * 
     * @param request CreateConcertRequestDTO mit Concert-Daten
     * @return ResponseEntity<ConcertResponseDTO> mit HTTP 201
     */
    @PostMapping
    @Operation(
        summary = "Neues Konzert erstellen",
        description = "Erstellt ein neues Konzert mit Name, Datum, Veranstaltungsort und optionaler Beschreibung. " +
                     "Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Konzert erfolgreich erstellt",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ConcertResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Ungültige Eingabedaten (z.B. leerer Name, Datum in der Vergangenheit)",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Zugriff verweigert (nicht ADMIN-Rolle)",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Interner Serverfehler",
            content = @Content
        )
    })
    public ResponseEntity<ConcertResponseDTO> createConcert(
        @Valid @RequestBody CreateConcertRequestDTO request
    ) {
        logger.info("Admin request: Creating new concert - {}", request.getName());
        
        try {
            ConcertResponseDTO response = concertApplicationService.createConcert(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            logger.error("Validation error: {}", ex.getMessage());
            throw ex;
        }
    }
    
    /**
     * PUT /api/concerts/{id}
     * 
     * Aktualisiert ein bestehendes Concert.
     * 
     * Acceptance Criteria (US-09):
     * - Endpoint: PUT /api/concerts/{id} ✅
     * - Nur ADMIN-Rolle ✅
     * - Response: ConcertResponseDTO ✅
     * - HTTP Status: 200 OK ✅
     * 
     * @param concertId ID des zu aktualisierenden Concerts
     * @param request UpdateConcertRequestDTO mit neuen Daten
     * @return ResponseEntity<ConcertResponseDTO> mit HTTP 200
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Konzert aktualisieren",
        description = "Aktualisiert die Daten eines bestehenden Konzerts (Name, Datum, Veranstaltungsort, Beschreibung). " +
                     "Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Konzert erfolgreich aktualisiert",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ConcertResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Ungültige Eingabedaten",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Konzert nicht gefunden",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Zugriff verweigert (nicht ADMIN-Rolle)",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Concurrency-Fehler (Concert wurde gleichzeitig aktualisiert)",
            content = @Content
        )
    })
    public ResponseEntity<ConcertResponseDTO> updateConcert(
        @Parameter(description = "Concert-ID", example = "1")
        @PathVariable(value = "id") Long concertId,
        @Valid @RequestBody UpdateConcertRequestDTO request
    ) {
        logger.info("Admin request: Updating concert ID - {}", concertId);
        
        try {
            ConcertResponseDTO response = concertApplicationService.updateConcert(concertId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            logger.error("Error: {}", ex.getMessage());
            throw ex;
        }
    }
    
    /**
     * DELETE /api/concerts/{id}
     * 
     * Löscht ein Concert und alle zugehörigen Seats.
     * 
     * WARNUNG: Diese Operation ist nicht rückgängig zu machen.
     * 
     * Acceptance Criteria (US-09):
     * - Endpoint: DELETE /api/concerts/{id} ✅
     * - Nur ADMIN-Rolle ✅
     * - HTTP Status: 204 No Content ✅
     * 
     * @param concertId ID des zu löschenden Concerts
     * @return ResponseEntity mit HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Konzert löschen",
        description = "Löscht ein Konzert und alle zugehörigen Sitzplätze. " +
                     "WARNUNG: Diese Operation ist nicht rückgängig zu machen. " +
                     "Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Konzert erfolgreich gelöscht"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Konzert nicht gefunden"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Zugriff verweigert (nicht ADMIN-Rolle)"
        )
    })
    public ResponseEntity<Void> deleteConcert(
        @Parameter(description = "Concert-ID", example = "1")
        @PathVariable(value = "id") Long concertId
    ) {
        logger.warn("Admin request: Deleting concert ID - {}", concertId);
        
        try {
            concertApplicationService.deleteConcert(concertId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            logger.error("Error: {}", ex.getMessage());
            throw ex;
        }
    }
    
    /**
     * PUT /api/concerts/{id}/seats
     * 
     * Ersetzt ALLE Sitzplätze eines Concerts (Delete + Create).
     * Löscht erst alle vorhandenen Sitze, dann erstellt die neuen.
     * 
     * @param concertId ID des Concerts
     * @param request CreateSeatsRequestDTO mit neuer Seat-Liste
     * @return ResponseEntity mit HTTP 200 OK
     */
    @PutMapping("/{id}/seats")
    @Operation(
        summary = "Sitzplätze für Konzert ersetzen",
        description = "Ersetzt ALLE Sitzplätze eines Konzerts. " +
                     "Löscht erst alle vorhandenen Sitze, dann erstellt die neuen. " +
                     "WARNUNG: Vorhandene Reservierungen werden ungültig! " +
                     "Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Sitzplätze erfolgreich ersetzt"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Ungültige Eingabedaten",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Konzert nicht gefunden",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Zugriff verweigert (nicht ADMIN-Rolle)",
            content = @Content
        )
    })
    public ResponseEntity<String> replaceSeats(
        @Parameter(description = "Concert-ID", example = "1")
        @PathVariable(value = "id") Long concertId,
        @Valid @RequestBody CreateSeatsRequestDTO request
    ) {
        logger.warn("Admin request: Replacing ALL seats for concert ID - {} with {} new seats", 
                   concertId, request.getSeats().size());
        
        try {
            concertApplicationService.replaceSeats(concertId, request.getSeats());
            
            String responseMessage = String.format(
                "Erfolgreich alle Sitzplätze für Konzert %d ersetzt. Neue Anzahl: %d",
                concertId,
                request.getSeats().size()
            );
            
            return ResponseEntity.ok(responseMessage);
        } catch (IllegalArgumentException ex) {
            logger.error("Error: {}", ex.getMessage());
            throw ex;
        }
    }

    /**
     * POST /api/concerts/{id}/seats
     * 
     * Erstellt mehrere Sitzplätze für ein Concert (Bulk Operation).
     * 
     * Acceptance Criteria (US-09):
     * - Endpoint: POST /api/concerts/{id}/seats (Bulk) ✅
     * - Initial seat status: AVAILABLE ✅
     * - Validierung: Mindestens 1 Seat erforderlich ✅
     * - Nur ADMIN-Rolle ✅
     * - HTTP Status: 201 Created ✅
     * 
     * Performance:
     * - Optimiert für Batch Processing (saveAll statt einzelne saves)
     * 
     * @param concertId ID des Concerts
     * @param request CreateSeatsRequestDTO mit Seat-Liste
     * @return ResponseEntity mit HTTP 201
     */
    @PostMapping("/{id}/seats")
    @Operation(
        summary = "Sitzplätze für Konzert erstellen (Bulk)",
        description = "Erstellt mehrere Sitzplätze für ein Konzert in einer Operation. " +
                     "Optimiert für Bulk-Operationen mit effizienter Datenbanknutzung. " +
                     "Alle Seats werden mit Status AVAILABLE erstellt. " +
                     "Mindestens 1 Seat erforderlich. " +
                     "Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Sitzplätze erfolgreich erstellt"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Ungültige Eingabedaten (z.B. keine Seats, negative Preise)",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Konzert nicht gefunden",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Zugriff verweigert (nicht ADMIN-Rolle)",
            content = @Content
        )
    })
    public ResponseEntity<String> createSeats(
        @Parameter(description = "Concert-ID", example = "1")
        @PathVariable(value = "id") Long concertId,
        @Valid @RequestBody CreateSeatsRequestDTO request
    ) {
        logger.info("Admin request: Creating {} seats for concert ID - {}", 
                   request.getSeats().size(), concertId);
        
        try {
            concertApplicationService.createSeats(concertId, request.getSeats());
            
            String responseMessage = String.format(
                "Erfolgreich %d Sitzplätze für Konzert %d erstellt",
                request.getSeats().size(),
                concertId
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(responseMessage);
        } catch (IllegalArgumentException ex) {
            logger.error("Error: {}", ex.getMessage());
            throw ex;
        }
    }
    
    /**
     * GET /api/concerts (Read-Only, keine Admin-Anforderung)
     * 
     * Ruft alle Concerts ab.
     * 
     * @return ResponseEntity mit List<ConcertResponseDTO>
     */
    @GetMapping
    @PreAuthorize("permitAll()")
    @Operation(
        summary = "Alle Konzerte abrufen",
        description = "Liefert eine paginierte Liste aller Konzerte mit Verfügbarkeitsindikator und Preisrange. Öffentlicher Zugriff."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Liste der Konzerte erfolgreich abgerufen",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PagedConcertResponseDTO.class)
            )
        )
    })
    @SuppressWarnings("null")
    public ResponseEntity<PagedConcertResponseDTO> getConcerts(
        @Parameter(description = "Filter nach Datum (YYYY-MM-DD)")
        @RequestParam(required = false) LocalDate date,
        @Parameter(description = "Filter nach Venue (Teilstring)")
        @RequestParam(required = false) String venue,
        @Parameter(description = "Mindestpreis")
        @RequestParam(required = false) Double minPrice,
        @Parameter(description = "Höchstpreis")
        @RequestParam(required = false) Double maxPrice,
        @Parameter(description = "Sortierfeld: date|name|price", example = "date")
        @RequestParam(defaultValue = "date") String sortBy,
        @Parameter(description = "Sortierreihenfolge: asc|desc", example = "asc")
        @RequestParam(defaultValue = "asc") String sortOrder,
        @Parameter(description = "Page (0-basiert)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Seitengröße", example = "20")
        @RequestParam(defaultValue = "20") int size
    ) {
        logger.debug("Fetching concerts with filters: date={}, venue={}, priceRange=[{},{}], sort={} {} page={} size={}",
            date, venue, minPrice, maxPrice, sortBy, sortOrder, page, size);

        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw new IllegalArgumentException("minPrice darf nicht größer als maxPrice sein");
        }

        ConcertFilterCriteria filter = new ConcertFilterCriteria(date, venue, minPrice, maxPrice);
        Sort sort = Objects.requireNonNullElse(buildSort(sortBy, sortOrder), Sort.unsorted());
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedConcertResponseDTO response = concertApplicationService.getConcerts(filter, pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/concerts/{id} (Read-Only, keine Admin-Anforderung)
     * 
     * Ruft ein Concert anhand der ID ab.
     * 
     * @param concertId ID des Concerts
     * @return ResponseEntity<ConcertResponseDTO>
     */
    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    @Operation(
        summary = "Konzert nach ID abrufen",
        description = "Liefert die Details eines spezifischen Konzerts. Öffentlicher Zugriff."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Concert erfolgreich abgerufen",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ConcertResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Concert nicht gefunden"
        )
    })
    public ResponseEntity<ConcertResponseDTO> getConcertById(
        @Parameter(description = "Concert-ID", example = "1")
        @PathVariable(value = "id") Long concertId
    ) {
        logger.debug("Fetching concert with ID: {}", concertId);
        
        ConcertResponseDTO concert = concertApplicationService.getConcertById(concertId);
        return ResponseEntity.ok(concert);
    }

    private Sort buildSort(String sortBy, String sortOrder) {
        String safeSortOrder = sortOrder == null ? "" : sortOrder;
        Sort.Direction direction = Objects.requireNonNullElse(
            Sort.Direction.fromOptionalString(safeSortOrder).orElse(Sort.Direction.ASC),
            Sort.Direction.ASC
        );

        String normalizedSort = sortBy == null ? "" : sortBy.toLowerCase();
        String property = switch (normalizedSort) {
            case "name" -> "name";
            case "price" -> "price";
            default -> "date";
        };

        return Sort.by(direction == null ? Sort.Direction.ASC : direction, property);
    }
    
    /**
     * GET /api/concerts/search?name=... (Read-Only, keine Admin-Anforderung)
     * 
     * Sucht Concerts nach Name.
     * 
     * @param name Name oder Teilstring
     * @return ResponseEntity mit List<ConcertResponseDTO>
     */
    @GetMapping("/search")
    @PreAuthorize("permitAll()")
    @Operation(
        summary = "Konzerte nach Name durchsuchen",
        description = "Sucht Konzerte nach Name (Teilstring-Suche, case-insensitive). Öffentlicher Zugriff."
    )
    public ResponseEntity<List<ConcertResponseDTO>> searchConcerts(
        @Parameter(description = "Name oder Teilstring", example = "Metallica")
        @RequestParam String name
    ) {
        logger.debug("Searching concerts by name: {}", name);
        
        List<ConcertResponseDTO> concerts = concertApplicationService.searchConcertsByName(name);
        return ResponseEntity.ok(concerts);
    }
}
