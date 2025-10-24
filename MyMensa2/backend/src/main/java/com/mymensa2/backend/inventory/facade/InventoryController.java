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
    
    // 13. Alle Zutaten abrufen
    @GetMapping
    public ResponseEntity<List<IngredientResponseDTO>> getAllIngredients(
            @RequestParam(required = false) Boolean lowStock) {
        List<IngredientResponseDTO> ingredients = inventoryService.getAllIngredients(lowStock);
        return ResponseEntity.ok(ingredients);
    }
    
    // 14. Zutat erstellen
    @PostMapping
    public ResponseEntity<IngredientResponseDTO> createIngredient(@RequestBody IngredientRequestDTO request) {
        IngredientResponseDTO ingredient = inventoryService.createIngredient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ingredient);
    }
    
    // 15. Zutat aktualisieren
    @PutMapping("/{id}")
    public ResponseEntity<IngredientResponseDTO> updateIngredient(
            @PathVariable Integer id,
            @RequestBody IngredientUpdateDTO request) {
        IngredientResponseDTO ingredient = inventoryService.updateIngredient(id, request);
        return ResponseEntity.ok(ingredient);
    }
    
    // 16. Automatische Nachbestellung auslösen (FOODSUPPLY-Integration)
    @PostMapping("/reorder")
    public ResponseEntity<ReorderResponseDTO> triggerReorder() {
        ReorderResponseDTO reorder = inventoryService.triggerReorder();
        return ResponseEntity.ok(reorder);
    }
    
    // 17. Lagerbestand nach Gerichtszubereitung aktualisieren
    @PutMapping("/consume")
    public ResponseEntity<ConsumeResponseDTO> consumeIngredients(@RequestBody ConsumeRequestDTO request) {
        ConsumeResponseDTO consume = inventoryService.consumeIngredients(request);
        return ResponseEntity.ok(consume);
    }
}
