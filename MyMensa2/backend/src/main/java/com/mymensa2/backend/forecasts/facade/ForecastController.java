package com.mymensa2.backend.forecasts.facade;

import com.mymensa2.backend.forecasts.logic.ForecastService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3001")
public class ForecastController {
    
    private final ForecastService forecastService;
    
    public ForecastController(ForecastService forecastService) {
        this.forecastService = forecastService;
    }
    
    // 23. Wareneinsatz-Prognose für Zeitraum
    @GetMapping("/forecasts/demand")
    public ResponseEntity<DemandForecastResponseDTO> getDemandForecast(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        DemandForecastResponseDTO forecast = forecastService.getDemandForecast(startDate, endDate);
        return ResponseEntity.ok(forecast);
    }
    
    // 24. Nachhaltigkeit-Bericht
    @GetMapping("/reports/sustainability")
    public ResponseEntity<SustainabilityReportDTO> getSustainabilityReport(
            @RequestParam Integer month,
            @RequestParam Integer year) {
        SustainabilityReportDTO report = forecastService.getSustainabilityReport(month, year);
        return ResponseEntity.ok(report);
    }
}
