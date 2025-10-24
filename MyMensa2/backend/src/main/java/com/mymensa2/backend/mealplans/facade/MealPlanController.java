package com.mymensa2.backend.mealplans.facade;

import com.mymensa2.backend.mealplans.logic.MealPlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meal-plans")
@CrossOrigin(origins = "http://localhost:3001")
public class MealPlanController {
    
    private final MealPlanService mealPlanService;
    
    public MealPlanController(MealPlanService mealPlanService) {
        this.mealPlanService = mealPlanService;
    }
    
    // 5. Speiseplan für einen Zeitraum abrufen
    @GetMapping
    public ResponseEntity<List<MealPlanDayDTO>> getMealPlansForDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        List<MealPlanDayDTO> mealPlans = mealPlanService.getMealPlansForDateRange(startDate, endDate);
        return ResponseEntity.ok(mealPlans);
    }
    
    // 6. Gericht zum Speiseplan hinzufügen oder Bestand aktualisieren
    @PutMapping
    public ResponseEntity<MealPlanResponseDTO> addOrUpdateMealPlan(@RequestBody MealPlanRequestDTO request) {
        MealPlanResponseDTO mealPlan = mealPlanService.addOrUpdateMealPlan(request);
        
        // Prüfe, ob es eine Erstellung oder ein Update war (durch Existenzprüfung)
        // Für Simplizität geben wir immer 200 OK zurück
        return ResponseEntity.ok(mealPlan);
    }
    
    // 7. Gericht aus Speiseplan entfernen
    @DeleteMapping
    public ResponseEntity<Void> removeMealFromPlan(
            @RequestParam Integer mealId,
            @RequestParam String date) {
        mealPlanService.removeMealFromPlan(mealId, date);
        return ResponseEntity.noContent().build();
    }
}
