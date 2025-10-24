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
    
    // 18. Alle Mitarbeiter abrufen
    @GetMapping
    public ResponseEntity<List<StaffResponseDTO>> getAllStaff(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean available) {
        List<StaffResponseDTO> staff = staffService.getAllStaff(role, available);
        return ResponseEntity.ok(staff);
    }
    
    // 19. Mitarbeiter erstellen
    @PostMapping
    public ResponseEntity<StaffResponseDTO> createStaff(@RequestBody StaffRequestDTO request) {
        StaffResponseDTO staff = staffService.createStaff(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(staff);
    }
    
    // 20. Arbeitszeiten erfassen
    @PostMapping("/{staffId}/working-hours")
    public ResponseEntity<WorkingHoursResponseDTO> recordWorkingHours(
            @PathVariable Integer staffId,
            @RequestBody WorkingHoursRequestDTO request) {
        WorkingHoursResponseDTO workingHours = staffService.recordWorkingHours(staffId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(workingHours);
    }
    
    // 21. Arbeitszeiten für Zeitraum abrufen
    @GetMapping("/working-hours")
    public ResponseEntity<List<WorkingHoursWithStaffDTO>> getWorkingHours(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) Integer staffId) {
        List<WorkingHoursWithStaffDTO> workingHours = staffService.getWorkingHours(startDate, endDate, staffId);
        return ResponseEntity.ok(workingHours);
    }
    
    // 22. Einsatzplanung basierend auf erwarteter Besucherzahl
    @GetMapping("/schedule-recommendation")
    public ResponseEntity<ScheduleRecommendationDTO> getScheduleRecommendation(@RequestParam String date) {
        ScheduleRecommendationDTO recommendation = staffService.getScheduleRecommendation(date);
        return ResponseEntity.ok(recommendation);
    }
}
