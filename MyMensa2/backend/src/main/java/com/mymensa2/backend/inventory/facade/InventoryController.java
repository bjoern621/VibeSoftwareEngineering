package com.mymensa2.backend.inventory.facade;

import com.mymensa2.backend.inventory.logic.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "http://localhost:3001")
public class InventoryController {
    
    private final InventoryService inventoryService;
    
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
    
    // Endpoint 13: Alle Zutaten abrufen
    @GetMapping
    public ResponseEntity<List<IngredientDTO>> getAllIngredients(
        @RequestParam(required = false) Boolean lowStock
    ) {
        List<IngredientDTO> ingredients = inventoryService.getAllIngredients(lowStock);
        return ResponseEntity.ok(ingredients);
    }
    
    // Endpoint 14: Zutat erstellen
    @PostMapping
    public ResponseEntity<IngredientDTO> createIngredient(@RequestBody IngredientRequestDTO request) {
        IngredientDTO created = inventoryService.createIngredient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    // Endpoint 15: Zutat aktualisieren
    @PutMapping("/{id}")
    public ResponseEntity<IngredientDTO> updateIngredient(
        @PathVariable Integer id,
        @RequestBody IngredientUpdateDTO update
    ) {
        IngredientDTO updated = inventoryService.updateIngredient(id, update);
        return ResponseEntity.ok(updated);
    }
    
    // Endpoint 16: Automatische Nachbestellung auslösen
    @PostMapping("/reorder")
    public ResponseEntity<ReorderResponseDTO> reorderLowStockItems() {
        ReorderResponseDTO response = inventoryService.reorderLowStockItems();
        return ResponseEntity.ok(response);
    }
}
