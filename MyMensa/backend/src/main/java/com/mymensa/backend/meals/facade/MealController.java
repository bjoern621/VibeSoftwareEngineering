package com.mymensa.backend.meals.facade;

import com.mymensa.backend.meals.logic.MealService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meals")
@CrossOrigin(origins = "http://localhost:3000")
public class MealController {
    
    @Autowired
    private MealService mealService;
    
    /**
     * GET /api/meals - Alle Gerichte abrufen
     */
    @GetMapping
    public ResponseEntity<List<MealDTO>> getAllMeals() {
        List<MealDTO> meals = mealService.getAllMeals();
        return ResponseEntity.ok(meals);
    }
    
    /**
     * POST /api/meals - Neues Gericht erstellen
     */
    @PostMapping
    public ResponseEntity<MealDTO> createMeal(@RequestBody MealDTO mealDTO) {
        MealDTO createdMeal = mealService.createMeal(mealDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMeal);
    }
    
    /**
     * PUT /api/meals/{id} - Gericht aktualisieren
     */
    @PutMapping("/{id}")
    public ResponseEntity<MealDTO> updateMeal(@PathVariable Integer id, @RequestBody MealDTO mealDTO) {  // Integer as per specification
        MealDTO updatedMeal = mealService.updateMeal(id, mealDTO);
        return ResponseEntity.ok(updatedMeal);
    }
    
    /**
     * DELETE /api/meals/{id} - Gericht löschen
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeal(@PathVariable Integer id) {  // Integer as per specification
        mealService.deleteMeal(id);
        return ResponseEntity.noContent().build();
    }
}
