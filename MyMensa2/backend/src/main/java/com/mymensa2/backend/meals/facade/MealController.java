package com.mymensa2.backend.meals.facade;

import com.mymensa2.backend.meals.logic.MealService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meals")
@CrossOrigin(origins = "http://localhost:3001")
public class MealController {
    
    private final MealService mealService;
    
    public MealController(MealService mealService) {
        this.mealService = mealService;
    }
    
    // 1. Alle aktiven Gerichte abrufen
    @GetMapping
    public ResponseEntity<List<MealResponseDTO>> getAllActiveMeals() {
        List<MealResponseDTO> meals = mealService.getAllActiveMeals();
        return ResponseEntity.ok(meals);
    }
    
    // 2. Neues Gericht erstellen
    @PostMapping
    public ResponseEntity<MealResponseDTO> createMeal(@RequestBody MealRequestDTO request) {
        MealResponseDTO meal = mealService.createMeal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(meal);
    }
    
    // 3. Gericht aktualisieren
    @PutMapping("/{id}")
    public ResponseEntity<MealResponseDTO> updateMeal(@PathVariable Integer id, @RequestBody MealRequestDTO request) {
        MealResponseDTO meal = mealService.updateMeal(id, request);
        return ResponseEntity.ok(meal);
    }
    
    // 4. Gericht löschen (Soft Delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeal(@PathVariable Integer id) {
        mealService.deleteMeal(id);
        return ResponseEntity.noContent().build();
    }
}
