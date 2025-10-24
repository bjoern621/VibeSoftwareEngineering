package com.mymensa2.backend.meals.logic;

import com.mymensa2.backend.common.InvalidRequestException;
import com.mymensa2.backend.common.ResourceNotFoundException;
import com.mymensa2.backend.meals.dataaccess.Meal;
import com.mymensa2.backend.meals.dataaccess.MealRepository;
import com.mymensa2.backend.meals.facade.MealRequestDTO;
import com.mymensa2.backend.meals.facade.MealResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MealService {
    
    private final MealRepository mealRepository;
    
    public MealService(MealRepository mealRepository) {
        this.mealRepository = mealRepository;
    }
    
    // Alle aktiven Gerichte abrufen
    @Transactional(readOnly = true)
    public List<MealResponseDTO> getAllActiveMeals() {
        return mealRepository.findAllActive().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // Neues Gericht erstellen
    @Transactional
    public MealResponseDTO createMeal(MealRequestDTO request) {
        validateMealRequest(request);
        
        Meal meal = new Meal(
            request.name(),
            request.description(),
            request.price(),
            request.cost(),
            request.ingredients(),
            request.nutritionalInfo(),
            request.categories(),
            request.allergens()
        );
        
        Meal savedMeal = mealRepository.save(meal);
        return convertToDTO(savedMeal);
    }
    
    // Gericht aktualisieren
    @Transactional
    public MealResponseDTO updateMeal(Integer id, MealRequestDTO request) {
        validateMealRequest(request);
        
        Meal meal = mealRepository.findByIdActive(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gericht mit ID " + id + " nicht gefunden"));
        
        meal.setName(request.name());
        meal.setDescription(request.description());
        meal.setPrice(request.price());
        meal.setCost(request.cost());
        meal.setIngredients(request.ingredients());
        meal.setNutritionalInfo(request.nutritionalInfo());
        meal.setCategories(request.categories());
        meal.setAllergens(request.allergens());
        
        Meal updatedMeal = mealRepository.save(meal);
        return convertToDTO(updatedMeal);
    }
    
    // Gericht löschen (Soft Delete)
    @Transactional
    public void deleteMeal(Integer id) {
        Meal meal = mealRepository.findByIdActive(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gericht mit ID " + id + " nicht gefunden"));
        
        meal.setDeleted(true);
        meal.setDeletedAt(LocalDateTime.now());
        mealRepository.save(meal);
    }
    
    // Gericht nach ID abrufen (auch gelöschte für historische Daten)
    @Transactional(readOnly = true)
    public Meal getMealByIdIncludingDeleted(Integer id) {
        return mealRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gericht mit ID " + id + " nicht gefunden"));
    }
    
    // Gericht nach ID abrufen (nur aktive)
    @Transactional(readOnly = true)
    public Meal getMealByIdActive(Integer id) {
        return mealRepository.findByIdActive(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gericht mit ID " + id + " nicht gefunden"));
    }
    
    // Validierung der Meal-Request-Daten
    private void validateMealRequest(MealRequestDTO request) {
        if (request.name() == null || request.name().trim().isEmpty()) {
            throw new InvalidRequestException("Name ist erforderlich");
        }
        if (request.description() == null || request.description().trim().isEmpty()) {
            throw new InvalidRequestException("Beschreibung ist erforderlich");
        }
        if (request.price() == null || request.price() <= 0) {
            throw new InvalidRequestException("Preis muss größer als 0 sein");
        }
        if (request.cost() == null || request.cost() <= 0) {
            throw new InvalidRequestException("Kosten müssen größer als 0 sein");
        }
        if (request.ingredients() == null || request.ingredients().trim().isEmpty()) {
            throw new InvalidRequestException("Zutaten sind erforderlich");
        }
        if (request.nutritionalInfo() == null) {
            throw new InvalidRequestException("Nährwertinformationen sind erforderlich");
        }
        if (request.categories() == null) {
            throw new InvalidRequestException("Kategorien sind erforderlich");
        }
        if (request.allergens() == null) {
            throw new InvalidRequestException("Allergene sind erforderlich");
        }
    }
    
    // Konvertierung von Entity zu DTO
    private MealResponseDTO convertToDTO(Meal meal) {
        return new MealResponseDTO(
            meal.getId(),
            meal.getName(),
            meal.getDescription(),
            meal.getPrice(),
            meal.getCost(),
            meal.getIngredients(),
            meal.getNutritionalInfo(),
            meal.getCategories(),
            meal.getAllergens(),
            meal.getDeleted(),
            meal.getDeletedAt()
        );
    }
}
