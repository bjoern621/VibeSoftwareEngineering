package com.mymensa2.backend.inventory.logic;

import com.mymensa2.backend.inventory.dataaccess.Ingredient;
import com.mymensa2.backend.inventory.dataaccess.IngredientRepository;
import com.mymensa2.backend.inventory.facade.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class InventoryService {
    
    private final IngredientRepository ingredientRepository;
    
    public InventoryService(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }
    
    // Alle Zutaten abrufen
    public List<IngredientDTO> getAllIngredients(Boolean lowStock) {
        List<Ingredient> ingredients;
        
        if (lowStock != null && lowStock) {
            ingredients = ingredientRepository.findLowStockIngredients();
        } else {
            ingredients = ingredientRepository.findAll();
        }
        
        return ingredients.stream()
            .map(this::toDTO)
            .toList();
    }
    
    // Neue Zutat erstellen
    @Transactional
    public IngredientDTO createIngredient(IngredientRequestDTO request) {
        Ingredient ingredient = new Ingredient(
            request.name(),
            request.unit(),
            request.stockQuantity(),
            request.minStockLevel(),
            request.pricePerUnit(),
            request.supplierId()
        );
        
        Ingredient saved = ingredientRepository.save(ingredient);
        return toDTO(saved);
    }
    
    // Zutat aktualisieren (z.B. Lagerbestand)
    @Transactional
    public IngredientDTO updateIngredient(Integer id, IngredientUpdateDTO update) {
        Ingredient ingredient = ingredientRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Zutat nicht gefunden"));
        
        if (update.stockQuantity() != null) {
            ingredient.setStockQuantity(update.stockQuantity());
        }
        
        Ingredient saved = ingredientRepository.save(ingredient);
        return toDTO(saved);
    }
    
    // Automatische Nachbestellung auslösen (vereinfachte FOODSUPPLY-Integration)
    @Transactional
    public ReorderResponseDTO reorderLowStockItems() {
        List<Ingredient> lowStockIngredients = ingredientRepository.findLowStockIngredients();
        
        List<ReorderedItemDTO> reorderedItems = new ArrayList<>();
        double totalOrderValue = 0.0;
        
        for (Ingredient ingredient : lowStockIngredients) {
            // Nachbestellmenge = Mindestbestand * 5 (Vereinfachung)
            Float reorderQuantity = ingredient.getMinStockLevel() * 5;
            
            // Simulierte FOODSUPPLY-Order-ID
            String foodsupplyOrderId = "FOODSUPPLY-ORD-" + System.currentTimeMillis() + "-" + ingredient.getId();
            
            // Bestand aktualisieren
            ingredient.setStockQuantity(ingredient.getStockQuantity() + reorderQuantity);
            ingredientRepository.save(ingredient);
            
            // Response-Objekt erstellen
            ReorderedItemDTO item = new ReorderedItemDTO(
                ingredient.getId(),
                ingredient.getName(),
                ingredient.getStockQuantity() - reorderQuantity, // alter Bestand
                ingredient.getMinStockLevel(),
                reorderQuantity,
                ingredient.getSupplierId(),
                foodsupplyOrderId
            );
            
            reorderedItems.add(item);
            totalOrderValue += reorderQuantity * ingredient.getPricePerUnit();
        }
        
        return new ReorderResponseDTO(reorderedItems, totalOrderValue);
    }
    
    // Hilfsmethode: Entity zu DTO
    private IngredientDTO toDTO(Ingredient ingredient) {
        return new IngredientDTO(
            ingredient.getId(),
            ingredient.getName(),
            ingredient.getUnit(),
            ingredient.getStockQuantity(),
            ingredient.getMinStockLevel(),
            ingredient.getPricePerUnit(),
            ingredient.getSupplierId(),
            ingredient.needsReorder()
        );
    }
}
