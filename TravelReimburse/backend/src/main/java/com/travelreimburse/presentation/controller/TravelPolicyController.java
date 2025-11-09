package com.travelreimburse.presentation.controller;

import com.travelreimburse.application.dto.CreateTravelPolicyDTO;
import com.travelreimburse.application.dto.TravelPolicyResponseDTO;
import com.travelreimburse.application.service.TravelPolicyService;
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
 * REST Controller für Reiserichtlinien (Travel Policies)
 * 
 * DDD: Presentation Layer - nur Mapping zwischen HTTP und Application Layer
 */
@RestController
@RequestMapping("/api/policies")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Reiserichtlinien", description = "Verwaltung von Reiserichtlinien mit Pauschalen und Limits")
public class TravelPolicyController {
    
    private final TravelPolicyService policyService;
    
    public TravelPolicyController(TravelPolicyService policyService) {
        this.policyService = policyService;
    }
    
    /**
     * Erstellt eine neue Reiserichtlinie
     * POST /api/policies
     */
    @Operation(summary = "Neue Reiserichtlinie erstellen",
               description = "Erstellt eine neue Policy mit Limits für verschiedene Ausgabenkategorien")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Policy erfolgreich erstellt"),
        @ApiResponse(responseCode = "400", description = "Ungültige Eingabedaten")
    })
    @PostMapping
    public ResponseEntity<TravelPolicyResponseDTO> createPolicy(
            @Valid @RequestBody CreateTravelPolicyDTO dto) {
        TravelPolicyResponseDTO response = policyService.createPolicy(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Ruft alle aktiven Policies ab
     * GET /api/policies/active
     */
    @Operation(summary = "Alle aktiven Policies abrufen")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste erfolgreich abgerufen")
    })
    @GetMapping("/active")
    public ResponseEntity<List<TravelPolicyResponseDTO>> getActivePolicies() {
        List<TravelPolicyResponseDTO> response = policyService.findAllActive();
        return ResponseEntity.ok(response);
    }
    
    /**
     * Ruft alle Policies ab (aktiv und inaktiv)
     * GET /api/policies
     */
    @Operation(summary = "Alle Policies abrufen")
    @GetMapping
    public ResponseEntity<List<TravelPolicyResponseDTO>> getAllPolicies() {
        List<TravelPolicyResponseDTO> response = policyService.findAll();
        return ResponseEntity.ok(response);
    }
    
    /**
     * Ruft eine Policy anhand ihrer ID ab
     * GET /api/policies/{id}
     */
    @Operation(summary = "Policy nach ID abrufen")
    @GetMapping("/{id}")
    public ResponseEntity<TravelPolicyResponseDTO> getPolicyById(@PathVariable Long id) {
        TravelPolicyResponseDTO response = policyService.findById(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Aktiviert eine Policy
     * POST /api/policies/{id}/activate
     */
    @Operation(summary = "Policy aktivieren")
    @PostMapping("/{id}/activate")
    public ResponseEntity<TravelPolicyResponseDTO> activatePolicy(@PathVariable Long id) {
        TravelPolicyResponseDTO response = policyService.activatePolicy(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Deaktiviert eine Policy
     * POST /api/policies/{id}/deactivate
     */
    @Operation(summary = "Policy deaktivieren")
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<TravelPolicyResponseDTO> deactivatePolicy(@PathVariable Long id) {
        TravelPolicyResponseDTO response = policyService.deactivatePolicy(id);
        return ResponseEntity.ok(response);
    }
}
