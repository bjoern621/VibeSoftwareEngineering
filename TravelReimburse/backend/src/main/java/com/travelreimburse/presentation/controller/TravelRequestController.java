package com.travelreimburse.presentation.controller;

import com.travelreimburse.application.dto.AddTravelLegDTO;
import com.travelreimburse.application.dto.CreateTravelRequestDTO;
import com.travelreimburse.application.dto.TravelLegResponseDTO;
import com.travelreimburse.application.dto.TravelRequestResponseDTO;
import com.travelreimburse.application.service.TravelRequestService;
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
 * REST Controller für Reiseanträge
 * Stellt HTTP-Endpunkte für das Reiseantragssystem bereit
 */
@RestController
@RequestMapping("/api/travel-requests")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Reiseanträge", description = "Verwaltung von Reiseanträgen mit Genehmigungsworkflow und HRIS-Validierung")
public class TravelRequestController {
    
    private final TravelRequestService travelRequestService;
    
    public TravelRequestController(TravelRequestService travelRequestService) {
        this.travelRequestService = travelRequestService;
    }
    
    /**
     * Erstellt einen neuen Reiseantrag im Status DRAFT
     * POST /api/travel-requests
     */
    @Operation(summary = "Neuen Reiseantrag erstellen", 
               description = "Erstellt einen Reiseantrag im Status DRAFT")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Reiseantrag erfolgreich erstellt"),
        @ApiResponse(responseCode = "400", description = "Ungültige Eingabedaten")
    })
    @PostMapping
    public ResponseEntity<TravelRequestResponseDTO> createTravelRequest(
            @Valid @RequestBody CreateTravelRequestDTO dto) {
        TravelRequestResponseDTO response = travelRequestService.createTravelRequest(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Ruft einen einzelnen Reiseantrag ab
     * GET /api/travel-requests/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TravelRequestResponseDTO> getTravelRequest(@PathVariable Long id) {
        TravelRequestResponseDTO response = travelRequestService.findById(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Ruft alle Reiseanträge eines Mitarbeiters ab
     * GET /api/travel-requests?employeeId={employeeId}
     */
    @Operation(summary = "Reiseanträge eines Mitarbeiters abrufen")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste erfolgreich abgerufen")
    })
    @GetMapping
    public ResponseEntity<List<TravelRequestResponseDTO>> getTravelRequestsByEmployee(
            @RequestParam Long employeeId) {
        List<TravelRequestResponseDTO> response = travelRequestService.findByEmployeeId(employeeId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Reicht einen Reiseantrag ein (DRAFT -> SUBMITTED)
     * POST /api/travel-requests/{id}/submit
     * Validiert automatisch gegen HRIS-Abwesenheiten
     */
    @Operation(summary = "Reiseantrag einreichen", 
               description = "Reicht einen Reiseantrag zur Genehmigung ein. Validiert automatisch gegen HRIS-Abwesenheiten (Urlaub, Krankheit).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reiseantrag erfolgreich eingereicht"),
        @ApiResponse(responseCode = "404", description = "Reiseantrag nicht gefunden"),
        @ApiResponse(responseCode = "409", description = "Konflikt mit bestehenden Abwesenheiten (HRIS). Details in Response enthalten."),
        @ApiResponse(responseCode = "400", description = "Reiseantrag ist nicht im Status DRAFT")
    })
    @PostMapping("/{id}/submit")
    public ResponseEntity<TravelRequestResponseDTO> submitTravelRequest(@PathVariable Long id) {
        TravelRequestResponseDTO response = travelRequestService.submitTravelRequest(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Ruft alle eingereichten Reiseanträge ab (zur Genehmigung)
     * GET /api/travel-requests/pending-approvals
     * Für Führungskräfte
     */
    @GetMapping("/pending-approvals")
    public ResponseEntity<List<TravelRequestResponseDTO>> getPendingApprovals() {
        List<TravelRequestResponseDTO> response = travelRequestService.findPendingApprovals();
        return ResponseEntity.ok(response);
    }

    /**
     * Genehmigt einen Reiseantrag (SUBMITTED -> APPROVED)
     * POST /api/travel-requests/{id}/approve
     * Für Führungskräfte
     */
    @Operation(summary = "Reiseantrag genehmigen", 
               description = "Genehmigt einen eingereichten Reiseantrag")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reiseantrag erfolgreich genehmigt"),
        @ApiResponse(responseCode = "404", description = "Reiseantrag nicht gefunden"),
        @ApiResponse(responseCode = "400", description = "Reiseantrag ist nicht im Status SUBMITTED")
    })
    @PostMapping("/{id}/approve")
    public ResponseEntity<TravelRequestResponseDTO> approveTravelRequest(
            @PathVariable Long id,
            @Valid @RequestBody com.travelreimburse.application.dto.ApprovalRequestDTO request) {
        TravelRequestResponseDTO response = travelRequestService.approveTravelRequest(
            id,
            request.approverId()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Lehnt einen Reiseantrag ab (SUBMITTED -> REJECTED)
     * POST /api/travel-requests/{id}/reject
     * Für Führungskräfte
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<TravelRequestResponseDTO> rejectTravelRequest(
            @PathVariable Long id,
            @Valid @RequestBody com.travelreimburse.application.dto.RejectionRequestDTO request) {
        TravelRequestResponseDTO response = travelRequestService.rejectTravelRequest(
            id,
            request.approverId(),
            request.reason()
        );
        return ResponseEntity.ok(response);
    }
    
    /**
     * Fügt einen oder mehrere Reiseabschnitte zu einem Reiseantrag hinzu
     * POST /api/travel-requests/{id}/travel-legs
     * Akzeptiert sowohl ein einzelnes Objekt als auch ein Array
     * Nur möglich im Status DRAFT
     */
    @PostMapping("/{id}/travel-legs")
    public ResponseEntity<List<TravelLegResponseDTO>> addTravelLegs(
            @PathVariable Long id,
            @Valid @RequestBody List<AddTravelLegDTO> requests) {
        List<TravelLegResponseDTO> response = travelRequestService.addTravelLegs(id, requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Gibt alle Reiseabschnitte eines Reiseantrags zurück
     * GET /api/travel-requests/{id}/travel-legs
     */
    @GetMapping("/{id}/travel-legs")
    public ResponseEntity<List<TravelLegResponseDTO>> getTravelLegs(@PathVariable Long id) {
        List<TravelLegResponseDTO> response = travelRequestService.getTravelLegsByRequestId(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Entfernt einen Reiseabschnitt von einem Reiseantrag
     * DELETE /api/travel-requests/{id}/travel-legs/{legId}
     * Nur möglich im Status DRAFT
     */
    @DeleteMapping("/{id}/travel-legs/{legId}")
    public ResponseEntity<Void> removeTravelLeg(
            @PathVariable Long id,
            @PathVariable Long legId) {
        travelRequestService.removeTravelLeg(id, legId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Erstellt einen Reiseantrag im Namen eines anderen Mitarbeiters
     * POST /api/travel-requests/on-behalf?assistantId={assistantId}
     * Kann nur von Assistenten ausgeführt werden
     */
    @Operation(summary = "Reiseantrag im Namen eines anderen erstellen",
               description = "Assistenten können Reiseanträge für beliebige Mitarbeiter erstellen")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Reiseantrag erfolgreich erstellt"),
        @ApiResponse(responseCode = "400", description = "Ungültige Eingabedaten"),
        @ApiResponse(responseCode = "403", description = "Keine Berechtigung (nur ASSISTANT-Rolle)")
    })
    @PostMapping("/on-behalf")
    public ResponseEntity<TravelRequestResponseDTO> createTravelRequestOnBehalf(
            @Valid @RequestBody CreateTravelRequestDTO dto,
            @RequestParam Long assistantId) {
        TravelRequestResponseDTO response = travelRequestService.createTravelRequestOnBehalf(dto, assistantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
