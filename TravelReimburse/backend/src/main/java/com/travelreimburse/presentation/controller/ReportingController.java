package com.travelreimburse.presentation.controller;

import com.travelreimburse.application.dto.BudgetOverviewDTO;
import com.travelreimburse.application.dto.CostCenterReportDTO;
import com.travelreimburse.application.dto.TravelStatisticsResponseDTO;
import com.travelreimburse.application.service.ReportingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller für HR-Reporting und Auswertungen
 * Stellt HTTP-Endpunkte für Statistiken und Reports bereit
 * 
 * DDD: Presentation Layer - nur Request/Response-Handling, keine Business-Logik!
 */
@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "http://localhost:3000")
public class ReportingController {

    private final ReportingService reportingService;

    public ReportingController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    /**
     * Endpoint: Reisestatistiken abrufen
     * GET /api/reports/statistics?startDate=2024-01-01&endDate=2024-12-31
     * 
     * @param startDate Startdatum (optional, Standard: vor 1 Jahr)
     * @param endDate Enddatum (optional, Standard: heute)
     * @return Statistik-DTO mit Reiseaufkommen, Kosten, Genehmigungsquoten
     */
    @GetMapping("/statistics")
    public ResponseEntity<TravelStatisticsResponseDTO> getTravelStatistics(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate startDate,
            
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate endDate) {
        
        TravelStatisticsResponseDTO statistics = reportingService.getTravelStatistics(
            startDate, 
            endDate
        );
        
        return ResponseEntity.ok(statistics);
    }

    /**
     * Endpoint: Budget-Übersicht abrufen
     * GET /api/reports/budget?allocated=50000&currency=EUR&startDate=2024-01-01&endDate=2024-12-31
     * 
     * @param allocated zugewiesenes Budget
     * @param currency Währung (EUR, USD, etc.)
     * @param startDate Startdatum (optional)
     * @param endDate Enddatum (optional)
     * @return Budget-DTO mit Auslastung, Überschreitung, verbleibendem Budget
     */
    @GetMapping("/budget")
    public ResponseEntity<BudgetOverviewDTO> getBudgetOverview(
            @RequestParam BigDecimal allocated,
            
            @RequestParam String currency,
            
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate startDate,
            
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate endDate) {
        
        BudgetOverviewDTO overview = reportingService.getBudgetOverview(
            allocated, 
            currency, 
            startDate, 
            endDate
        );
        
        return ResponseEntity.ok(overview);
    }

    /**
     * Endpoint: Kostenstellen-Reports abrufen
     * GET /api/reports/cost-centers?startDate=2024-01-01&endDate=2024-12-31
     * 
     * Hinweis: Aktuell gruppiert nach EmployeeId (vereinfacht)
     * In Realität würde man nach echten Kostenstellen gruppieren
     * 
     * @param startDate Startdatum (optional)
     * @param endDate Enddatum (optional)
     * @return Liste von Kostenstellen-Reports
     */
    @GetMapping("/cost-centers")
    public ResponseEntity<List<CostCenterReportDTO>> getCostCenterReports(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate startDate,
            
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate endDate) {
        
        List<CostCenterReportDTO> reports = reportingService.getCostCenterReports(
            startDate, 
            endDate
        );
        
        return ResponseEntity.ok(reports);
    }

    /**
     * Endpoint: Zusammenfassung aller Reports (für HR-Dashboard)
     * GET /api/reports/summary?startDate=2024-01-01&endDate=2024-12-31
     * 
     * @param startDate Startdatum (optional)
     * @param endDate Enddatum (optional)
     * @return Map mit allen Reports
     */
    @GetMapping("/summary")
    public ResponseEntity<ReportSummaryDTO> getReportSummary(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate startDate,
            
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate endDate,
            
            @RequestParam(required = false, defaultValue = "50000") 
            BigDecimal defaultBudget,
            
            @RequestParam(required = false, defaultValue = "EUR") 
            String currency) {
        
        // Orchestration: Alle Reports zusammenstellen
        TravelStatisticsResponseDTO statistics = reportingService.getTravelStatistics(
            startDate, 
            endDate
        );
        
        BudgetOverviewDTO budget = reportingService.getBudgetOverview(
            defaultBudget, 
            currency, 
            startDate, 
            endDate
        );
        
        List<CostCenterReportDTO> costCenters = reportingService.getCostCenterReports(
            startDate, 
            endDate
        );
        
        ReportSummaryDTO summary = new ReportSummaryDTO(statistics, budget, costCenters);
        
        return ResponseEntity.ok(summary);
    }

    /**
     * DTO für Report-Zusammenfassung (nested DTO)
     */
    public record ReportSummaryDTO(
        TravelStatisticsResponseDTO statistics,
        BudgetOverviewDTO budget,
        List<CostCenterReportDTO> costCenters
    ) {}
}
