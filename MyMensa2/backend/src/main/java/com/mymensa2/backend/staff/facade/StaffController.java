package com.mymensa2.backend.staff.facade;

import com.mymensa2.backend.staff.logic.StaffService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
@CrossOrigin(origins = "http://localhost:3001")
public class StaffController {
    
    private final StaffService staffService;
    
    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }
    
    // Endpoint 18: Alle Mitarbeiter abrufen
    @GetMapping
    public ResponseEntity<List<StaffDTO>> getAllStaff(
        @RequestParam(required = false) String role,
        @RequestParam(required = false) Boolean available
    ) {
        List<StaffDTO> staff = staffService.getAllStaff(role, available);
        return ResponseEntity.ok(staff);
    }
    
    // Endpoint 19: Mitarbeiter erstellen
    @PostMapping
    public ResponseEntity<StaffDTO> createStaff(@RequestBody StaffRequestDTO request) {
        StaffDTO created = staffService.createStaff(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    // Endpoint 20: Arbeitszeiten erfassen
    @PostMapping("/{staffId}/working-hours")
    public ResponseEntity<WorkingHoursDTO> recordWorkingHours(
        @PathVariable Integer staffId,
        @RequestBody WorkingHoursRequestDTO request
    ) {
        WorkingHoursDTO recorded = staffService.recordWorkingHours(staffId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(recorded);
    }
    
    // Endpoint 21: Arbeitszeiten für Zeitraum abrufen
    @GetMapping("/working-hours")
    public ResponseEntity<List<WorkingHoursDTO>> getWorkingHours(
        @RequestParam String startDate,
        @RequestParam String endDate,
        @RequestParam(required = false) Integer staffId
    ) {
        List<WorkingHoursDTO> workingHours = staffService.getWorkingHours(startDate, endDate, staffId);
        return ResponseEntity.ok(workingHours);
    }
    
    // Endpoint 22: Einsatzplanung-Empfehlung
    @GetMapping("/schedule-recommendation")
    public ResponseEntity<ScheduleRecommendationDTO> getScheduleRecommendation(
        @RequestParam String date
    ) {
        ScheduleRecommendationDTO recommendation = staffService.getScheduleRecommendation(date);
        return ResponseEntity.ok(recommendation);
    }
}
