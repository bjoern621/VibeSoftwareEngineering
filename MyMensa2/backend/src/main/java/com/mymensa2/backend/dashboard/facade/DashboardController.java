package com.mymensa2.backend.dashboard.facade;

import com.mymensa2.backend.dashboard.logic.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:3001")
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }
    
    // 12. Dashboard-Daten abrufen
    @GetMapping
    public ResponseEntity<DashboardResponseDTO> getDashboardData(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        DashboardResponseDTO dashboard = dashboardService.getDashboardData(startDate, endDate);
        return ResponseEntity.ok(dashboard);
    }
}
