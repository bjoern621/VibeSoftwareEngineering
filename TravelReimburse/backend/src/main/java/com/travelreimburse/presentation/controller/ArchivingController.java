package com.travelreimburse.presentation.controller;

import com.travelreimburse.application.service.ArchivingService;
import com.travelreimburse.domain.model.TravelRequest;
import com.travelreimburse.presentation.dto.ArchiveRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/archiving")
@Tag(name = "Archivierung", description = "Verwaltung archivierter Reiseanträge")
public class ArchivingController {
    
    private final ArchivingService archivingService;
    
    public ArchivingController(ArchivingService archivingService) {
        this.archivingService = archivingService;
    }
    
    @PostMapping("/travel-requests/{id}")
    @Operation(summary = "Reiseantrag archivieren (Standard-Frist 10 Jahre)")
    public ResponseEntity<TravelRequest> archiveTravelRequest(@PathVariable Long id) {
        TravelRequest archived = archivingService.archiveTravelRequest(id);
        return ResponseEntity.ok(archived);
    }
    
    @PostMapping("/travel-requests/{id}/custom")
    @Operation(summary = "Reiseantrag mit benutzerdefinierter Frist archivieren")
    public ResponseEntity<TravelRequest> archiveWithCustomRetention(
        @PathVariable Long id,
        @Valid @RequestBody ArchiveRequestDTO dto
    ) {
        TravelRequest archived = archivingService.archiveWithCustomRetention(id, dto.retentionYears());
        return ResponseEntity.ok(archived);
    }
    
    @PostMapping("/batch")
    @Operation(summary = "Alle bereiten Reisen archivieren (Batch-Job)")
    public ResponseEntity<Integer> archiveAllReady() {
        int count = archivingService.archiveAllReadyRequests();
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/expired")
    @Operation(summary = "Finde Reisen mit abgelaufener Aufbewahrungsfrist")
    public ResponseEntity<List<TravelRequest>> findExpired() {
        List<TravelRequest> expired = archivingService.findExpiredRetentionRequests();
        return ResponseEntity.ok(expired);
    }
    
    @GetMapping
    @Operation(summary = "Archivierte Reisen in Zeitraum")
    public ResponseEntity<List<TravelRequest>> findArchived(
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate
    ) {
        List<TravelRequest> archived = archivingService.findArchivedInPeriod(startDate, endDate);
        return ResponseEntity.ok(archived);
    }
}

