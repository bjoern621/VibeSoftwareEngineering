package com.travelreimburse.presentation.controller;

import com.travelreimburse.application.dto.CreateTravelRequestDTO;
import com.travelreimburse.application.dto.TravelRequestResponseDTO;
import com.travelreimburse.application.service.TravelRequestService;
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
public class TravelRequestController {
    
    private final TravelRequestService travelRequestService;
    
    public TravelRequestController(TravelRequestService travelRequestService) {
        this.travelRequestService = travelRequestService;
    }
    
    /**
     * Erstellt einen neuen Reiseantrag im Status DRAFT
     * POST /api/travel-requests
     */
    @PostMapping
    public ResponseEntity<TravelRequestResponseDTO> createTravelRequest(
            @Valid @RequestBody CreateTravelRequestDTO request) {
        TravelRequestResponseDTO response = travelRequestService.createTravelRequest(request);
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
    @GetMapping
    public ResponseEntity<List<TravelRequestResponseDTO>> getTravelRequestsByEmployee(
            @RequestParam Long employeeId) {
        List<TravelRequestResponseDTO> response = travelRequestService.findByEmployeeId(employeeId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Reicht einen Reiseantrag ein (DRAFT -> SUBMITTED)
     * POST /api/travel-requests/{id}/submit
     */
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
}
