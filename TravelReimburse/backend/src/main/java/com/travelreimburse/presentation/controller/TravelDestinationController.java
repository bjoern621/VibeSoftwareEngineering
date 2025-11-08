package com.travelreimburse.presentation.controller;

import com.travelreimburse.application.dto.*;
import com.travelreimburse.application.service.TravelDestinationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller für Reiseziele mit Visa- und Impfanforderungen
 * 
 * DDD: Presentation Layer - nur Mapping zwischen HTTP und Application Layer
 */
@RestController
@RequestMapping("/api/destinations")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Reiseziele", description = "Verwaltung von Reisezielen mit Visa- und Impfanforderungen")
public class TravelDestinationController {

    private final TravelDestinationService service;

    public TravelDestinationController(TravelDestinationService service) {
        this.service = service;
    }

    /**
     * Erstellt ein neues Reiseziel
     * POST /api/destinations
     */
    @Operation(summary = "Reiseziel erstellen", 
               description = "Erstellt ein neues Reiseziel mit Visa- und Impfanforderungen")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Reiseziel erfolgreich erstellt"),
        @ApiResponse(responseCode = "400", description = "Ungültige Daten"),
        @ApiResponse(responseCode = "409", description = "Reiseziel für diesen Ländercode existiert bereits")
    })
    @PostMapping
    public ResponseEntity<TravelDestinationResponseDTO> createDestination(
        @Valid @RequestBody CreateTravelDestinationDTO request
    ) {
        TravelDestinationResponseDTO response = service.createDestination(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Ruft alle Reiseziele ab
     * GET /api/destinations
     */
    @Operation(summary = "Alle Reiseziele abrufen")
    @ApiResponse(responseCode = "200", description = "Liste aller Reiseziele")
    @GetMapping
    public ResponseEntity<List<TravelDestinationResponseDTO>> getAllDestinations() {
        List<TravelDestinationResponseDTO> destinations = service.getAllDestinations();
        return ResponseEntity.ok(destinations);
    }

    /**
     * Ruft ein Reiseziel anhand des Ländercodes ab
     * GET /api/destinations/country/{countryCode}
     */
    @Operation(summary = "Reiseziel nach Ländercode abrufen",
               description = "Findet ein Reiseziel anhand des ISO-Ländercodes (z.B. US, CN, IN)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reiseziel gefunden"),
        @ApiResponse(responseCode = "404", description = "Reiseziel nicht gefunden")
    })
    @GetMapping("/country/{countryCode}")
    public ResponseEntity<TravelDestinationResponseDTO> getDestinationByCountryCode(
        @PathVariable String countryCode
    ) {
        TravelDestinationResponseDTO destination = service.getDestinationByCountryCode(countryCode);
        return ResponseEntity.ok(destination);
    }

    /**
     * Ruft ein Reiseziel anhand der ID ab
     * GET /api/destinations/{id}
     */
    @Operation(summary = "Reiseziel nach ID abrufen")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reiseziel gefunden"),
        @ApiResponse(responseCode = "404", description = "Reiseziel nicht gefunden")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TravelDestinationResponseDTO> getDestinationById(@PathVariable Long id) {
        TravelDestinationResponseDTO destination = service.getDestinationById(id);
        return ResponseEntity.ok(destination);
    }

    /**
     * Ruft alle Reiseziele mit Visa-Pflicht ab
     * GET /api/destinations/visa-required
     */
    @Operation(summary = "Reiseziele mit Visa-Pflicht abrufen",
               description = "Findet alle Reiseziele die ein Visum benötigen")
    @ApiResponse(responseCode = "200", description = "Liste der Reiseziele mit Visa-Pflicht")
    @GetMapping("/visa-required")
    public ResponseEntity<List<TravelDestinationResponseDTO>> getDestinationsRequiringVisa() {
        List<TravelDestinationResponseDTO> destinations = service.getDestinationsRequiringVisa();
        return ResponseEntity.ok(destinations);
    }

    /**
     * Ruft alle Reiseziele mit Impfpflicht ab
     * GET /api/destinations/vaccination-required
     */
    @Operation(summary = "Reiseziele mit Impfpflicht abrufen",
               description = "Findet alle Reiseziele die Impfungen benötigen")
    @ApiResponse(responseCode = "200", description = "Liste der Reiseziele mit Impfpflicht")
    @GetMapping("/vaccination-required")
    public ResponseEntity<List<TravelDestinationResponseDTO>> getDestinationsRequiringVaccination() {
        List<TravelDestinationResponseDTO> destinations = service.getDestinationsRequiringVaccination();
        return ResponseEntity.ok(destinations);
    }

    /**
     * Aktualisiert die Visa-Anforderungen
     * PUT /api/destinations/{countryCode}/visa
     */
    @Operation(summary = "Visa-Anforderungen aktualisieren")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Visa-Anforderungen aktualisiert"),
        @ApiResponse(responseCode = "404", description = "Reiseziel nicht gefunden"),
        @ApiResponse(responseCode = "400", description = "Ungültige Daten")
    })
    @PutMapping("/{countryCode}/visa")
    public ResponseEntity<TravelDestinationResponseDTO> updateVisaRequirement(
        @PathVariable String countryCode,
        @Valid @RequestBody UpdateVisaRequirementDTO request
    ) {
        TravelDestinationResponseDTO response = service.updateVisaRequirement(countryCode, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Aktualisiert die Impfanforderungen
     * PUT /api/destinations/{countryCode}/vaccination
     */
    @Operation(summary = "Impfanforderungen aktualisieren")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Impfanforderungen aktualisiert"),
        @ApiResponse(responseCode = "404", description = "Reiseziel nicht gefunden"),
        @ApiResponse(responseCode = "400", description = "Ungültige Daten")
    })
    @PutMapping("/{countryCode}/vaccination")
    public ResponseEntity<TravelDestinationResponseDTO> updateVaccinationRequirement(
        @PathVariable String countryCode,
        @Valid @RequestBody UpdateVaccinationRequirementDTO request
    ) {
        TravelDestinationResponseDTO response = service.updateVaccinationRequirement(countryCode, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Aktualisiert allgemeine Reisehinweise
     * PUT /api/destinations/{countryCode}/advice
     */
    @Operation(summary = "Reisehinweise aktualisieren")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reisehinweise aktualisiert"),
        @ApiResponse(responseCode = "404", description = "Reiseziel nicht gefunden")
    })
    @PutMapping("/{countryCode}/advice")
    public ResponseEntity<TravelDestinationResponseDTO> updateTravelAdvice(
        @PathVariable String countryCode,
        @RequestBody String advice
    ) {
        TravelDestinationResponseDTO response = service.updateTravelAdvice(countryCode, advice);
        return ResponseEntity.ok(response);
    }

    /**
     * Löscht ein Reiseziel
     * DELETE /api/destinations/{id}
     */
    @Operation(summary = "Reiseziel löschen")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Reiseziel gelöscht"),
        @ApiResponse(responseCode = "404", description = "Reiseziel nicht gefunden")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDestination(@PathVariable Long id) {
        service.deleteDestination(id);
        return ResponseEntity.noContent().build();
    }
}
