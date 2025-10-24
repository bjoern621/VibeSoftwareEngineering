package com.mymensa2.backend.inventory.logic;

import com.mymensa2.backend.common.InvalidRequestException;
import com.mymensa2.backend.common.ResourceNotFoundException;
import com.mymensa2.backend.inventory.dataaccess.Ingredient;
import com.mymensa2.backend.inventory.dataaccess.IngredientRepository;
import com.mymensa2.backend.inventory.facade.*;
import com.mymensa2.backend.meals.dataaccess.Meal;
import com.mymensa2.backend.meals.logic.MealService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InventoryService {
    
    private final IngredientRepository ingredientRepository;
    private final MealService mealService;
    
    public InventoryService(IngredientRepository ingredientRepository, MealService mealService) {
        this.ingredientRepository = ingredientRepository;
        this.mealService = mealService;
    }
    
    // Alle Zutaten abrufen
    @Transactional(readOnly = true)
    public List<IngredientResponseDTO> getAllIngredients(Boolean lowStock) {
        List<Ingredient> ingredients;
        
        if (lowStock != null && lowStock) {
            ingredients = ingredientRepository.findLowStock();
        } else {
            ingredients = ingredientRepository.findAll();
        }
        
        return ingredients.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // Zutat erstellen
    @Transactional
    public IngredientResponseDTO createIngredient(IngredientRequestDTO request) {
        validateIngredientRequest(request);
        
        Ingredient ingredient = new Ingredient(
                request.name(),
                request.unit(),
                request.stockQuantity(),
                request.minStockLevel(),
                request.pricePerUnit(),
                request.supplierId()
        );
        
        Ingredient savedIngredient = ingredientRepository.save(ingredient);
        return convertToDTO(savedIngredient);
    }
    
    // Zutat aktualisieren
    @Transactional
    public IngredientResponseDTO updateIngredient(Integer id, IngredientUpdateDTO request) {
        if (request.stockQuantity() == null || request.stockQuantity() < 0) {
            throw new InvalidRequestException("Bestand muss 0 oder größer sein");
        }
        
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zutat mit ID " + id + " nicht gefunden"));
        
        ingredient.setStockQuantity(request.stockQuantity());
        
        Ingredient updatedIngredient = ingredientRepository.save(ingredient);
        return convertToDTO(updatedIngredient);
    }
    
    // Automatische Nachbestellung auslösen (FOODSUPPLY-Integration)
    @Transactional
    public ReorderResponseDTO triggerReorder() {
        List<Ingredient> lowStockIngredients = ingredientRepository.findLowStock();
        
        if (lowStockIngredients.isEmpty()) {
            return new ReorderResponseDTO(new ArrayList<>(), 0.0);
        }
        
        List<ReorderedItemDTO> reorderedItems = new ArrayList<>();
        double totalOrderValue = 0.0;
        
        for (Ingredient ingredient : lowStockIngredients) {
            // Berechne Nachbestellmenge (z.B. 50 Einheiten oder doppelter Mindestbestand)
            float reorderQuantity = Math.max(50.0f, ingredient.getMinStockLevel() * 2);
            
            // Simuliere FOODSUPPLY-Bestellung mit generierter Order-ID
            String foodsupplyOrderId = "FOODSUPPLY-ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            
            // Aktualisiere Bestand nach Bestellung
            ingredient.setStockQuantity(ingredient.getStockQuantity() + reorderQuantity);
            ingredientRepository.save(ingredient);
            
            double orderValue = reorderQuantity * ingredient.getPricePerUnit();
            totalOrderValue += orderValue;
            
            reorderedItems.add(new ReorderedItemDTO(
                    ingredient.getId(),
                    ingredient.getName(),
                    ingredient.getStockQuantity() - reorderQuantity, // Alter Bestand
                    ingredient.getMinStockLevel(),
                    reorderQuantity,
                    ingredient.getSupplierId(),
                    foodsupplyOrderId
            ));
        }
        
        return new ReorderResponseDTO(reorderedItems, totalOrderValue);
    }
    
    // Lagerbestand nach Gerichtszubereitung aktualisieren
    @Transactional
    public ConsumeResponseDTO consumeIngredients(ConsumeRequestDTO request) {
        if (request.mealId() == null) {
            throw new InvalidRequestException("Gericht-ID ist erforderlich");
        }
        if (request.quantity() == null || request.quantity() <= 0) {
            throw new InvalidRequestException("Menge muss größer als 0 sein");
        }
        
        Meal meal = mealService.getMealByIdActive(request.mealId());
        
        // Simuliere Zutatenverbrauch (vereinfacht - in Realität würde man Rezepturen haben)
        // Für Demo-Zwecke nehmen wir an, dass wir nur einige Zutaten verbrauchen
        List<ConsumedIngredientDTO> consumedIngredients = new ArrayList<>();
        
        // Beispiel: Verbrauche Zutaten basierend auf vereinfachter Logik
        // In der Realität würde man hier eine Meal-Ingredient-Mapping-Tabelle haben
        
        return new ConsumeResponseDTO(
                meal.getName(),
                request.quantity(),
                consumedIngredients
        );
    }
    
    // Validierung der Ingredient-Request-Daten
    private void validateIngredientRequest(IngredientRequestDTO request) {
        if (request.name() == null || request.name().trim().isEmpty()) {
            throw new InvalidRequestException("Name ist erforderlich");
        }
        if (request.unit() == null || request.unit().trim().isEmpty()) {
            throw new InvalidRequestException("Einheit ist erforderlich");
        }
        if (request.stockQuantity() == null || request.stockQuantity() < 0) {
            throw new InvalidRequestException("Bestand muss 0 oder größer sein");
        }
        if (request.minStockLevel() == null || request.minStockLevel() < 0) {
            throw new InvalidRequestException("Mindestbestand muss 0 oder größer sein");
        }
        if (request.pricePerUnit() == null || request.pricePerUnit() <= 0) {
            throw new InvalidRequestException("Preis pro Einheit muss größer als 0 sein");
        }
        if (request.supplierId() == null || request.supplierId().trim().isEmpty()) {
            throw new InvalidRequestException("Lieferanten-ID ist erforderlich");
        }
    }
    
    // Konvertierung von Entity zu DTO
    private IngredientResponseDTO convertToDTO(Ingredient ingredient) {
        return new IngredientResponseDTO(
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
