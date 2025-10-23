package com.mymensa2.backend.mealplans.facade;

import com.mymensa2.backend.mealplans.logic.MealPlanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller für MealPlan-Verwaltung
 * 
 * Endpoints:
 * - GET /api/meal-plans?startDate={date}&endDate={date} - Speiseplan für Zeitraum abrufen
 * - PUT /api/meal-plans - Gericht zum Speiseplan hinzufügen/aktualisieren
 * - DELETE /api/meal-plans?mealId={id}&date={date} - Gericht aus Speiseplan entfernen
 */
@RestController
@RequestMapping("/api/meal-plans")
@CrossOrigin(origins = "http://localhost:3001")
public class MealPlanController {
    
    @Autowired
    private MealPlanService mealPlanService;
    
    /**
     * GET /api/meal-plans?startDate={date}&endDate={date}
     * Speiseplan für einen Zeitraum abrufen
     * 
     * @param startDate Startdatum im Format YYYY-MM-DD
     * @param endDate Enddatum im Format YYYY-MM-DD
     * @return Liste von MealPlanDayDTO für jeden Tag im Zeitraum
     */
    @GetMapping
    public ResponseEntity<List<MealPlanDayDTO>> getMealPlansForDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        List<MealPlanDayDTO> mealPlans = mealPlanService.getMealPlansForDateRange(startDate, endDate);
        return ResponseEntity.ok(mealPlans);
    }
    
    /**
     * PUT /api/meal-plans
     * Gericht zum Speiseplan hinzufügen oder Bestand aktualisieren
     * 
     * @param requestDTO MealPlanRequestDTO mit mealId, date, stock
     * @return MealPlanResponseDTO mit vollständigen Informationen
     *         201 Created wenn neuer Eintrag, 200 OK wenn Update
     */
    @PutMapping
    public ResponseEntity<MealPlanResponseDTO> addOrUpdateMealPlan(
            @Valid @RequestBody MealPlanRequestDTO requestDTO) {

        var result = mealPlanService.addOrUpdateMealPlan(requestDTO);
        
        if (result.isCreated()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(result.getResponse());
        } else {
            return ResponseEntity.ok(result.getResponse());
        }
    }
    
    /**
     * DELETE /api/meal-plans?mealId={id}&date={date}
     * Gericht aus Speiseplan entfernen
     * 
     * @param mealId ID des zu entfernenden Gerichts
     * @param date Datum im Format YYYY-MM-DD
     * @return 204 No Content
     */
    @DeleteMapping
    public ResponseEntity<Void> removeMealFromPlan(
            @RequestParam Integer mealId,
            @RequestParam String date) {
        
        mealPlanService.removeMealFromPlan(mealId, date);
        return ResponseEntity.noContent().build();
    }
}
