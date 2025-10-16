package com.mymensa.backend.dashboard.facade;

import com.mymensa.backend.dashboard.logic.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller für Dashboard-API
 * Stellt Endpoint für Einnahmen/Ausgaben-Daten bereit
 */
@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:3000")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * GET /api/dashboard - Dashboard-Daten abrufen
     *
     * Liefert:
     * - Gesamteinnahmen
     * - Gesamtausgaben
     * - Gewinn
     * - Einnahmen pro Gericht
     * - Ausgaben pro Gericht
     *
     * @return DashboardDTO mit allen Dashboard-Daten
     */
    @GetMapping
    public ResponseEntity<DashboardDTO> getDashboardData() {
        DashboardDTO dashboardData = dashboardService.getDashboardData();
        return ResponseEntity.ok(dashboardData);
    }
}
