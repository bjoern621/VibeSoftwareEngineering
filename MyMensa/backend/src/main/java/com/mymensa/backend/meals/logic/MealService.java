package com.mymensa.backend.meals.logic;

import com.mymensa.backend.meals.common.InvalidRequestException;
import com.mymensa.backend.meals.common.ResourceNotFoundException;
import com.mymensa.backend.meals.dataaccess.Meal;
import com.mymensa.backend.meals.dataaccess.NutritionalInfo;
import com.mymensa.backend.meals.facade.MealDTO;
import com.mymensa.backend.meals.dataaccess.MealRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MealService {
    
    @Autowired
    private MealRepository mealRepository;
    
    /**
     * Alle Gerichte abrufen
     */
    public List<MealDTO> getAllMeals() {
        return mealRepository.findAll()
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Gericht nach ID abrufen
     */
    public MealDTO getMealById(Integer id) {  // Integer as per specification
        Meal meal = mealRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Gericht mit ID " + id + " nicht gefunden"));
        return convertToDTO(meal);
    }
    
    /**
     * Neues Gericht erstellen
     */
    public MealDTO createMeal(MealDTO mealDTO) {
        validateMealDTO(mealDTO);
        Meal meal = convertToEntity(mealDTO);
        Meal savedMeal = mealRepository.save(meal);
        return convertToDTO(savedMeal);
    }
    
    /**
     * Gericht aktualisieren
     */
    public MealDTO updateMeal(Integer id, MealDTO mealDTO) {  // Integer as per specification
        Meal existingMeal = mealRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Gericht mit ID " + id + " nicht gefunden"));
        
        validateMealDTO(mealDTO);
        
        // Update fields - Records use accessor methods without 'get' prefix
        existingMeal.setName(mealDTO.name());
        existingMeal.setDescription(mealDTO.description());
        existingMeal.setPrice(mealDTO.price());
        existingMeal.setCost(mealDTO.cost());
        existingMeal.setStock(mealDTO.stock());
        existingMeal.setIngredients(mealDTO.ingredients());
        existingMeal.setNutritionalInfo(convertToNutritionalInfoEntity(mealDTO.nutritionalInfo()));
        existingMeal.setCategories(mealDTO.categories());
        existingMeal.setAllergens(mealDTO.allergens());
        
        Meal updatedMeal = mealRepository.save(existingMeal);
        return convertToDTO(updatedMeal);
    }
    
    /**
     * Gericht löschen
     */
    public void deleteMeal(Integer id) {  // Integer as per specification
        if (!mealRepository.existsById(id)) {
            throw new ResourceNotFoundException("Gericht mit ID " + id + " nicht gefunden");
        }
        mealRepository.deleteById(id);
    }
    
    /**
     * Validierung der MealDTO
     * Records use accessor methods without 'get' prefix
     */
    private void validateMealDTO(MealDTO mealDTO) {
        if (mealDTO.name() == null || mealDTO.name().trim().isEmpty()) {
            throw new InvalidRequestException("Name ist erforderlich");
        }
        if (mealDTO.description() == null || mealDTO.description().trim().isEmpty()) {
            throw new InvalidRequestException("Beschreibung ist erforderlich");
        }
        if (mealDTO.price() == null || mealDTO.price() <= 0) {
            throw new InvalidRequestException("Preis muss größer als 0 sein");
        }
        if (mealDTO.cost() == null || mealDTO.cost() < 0) {
            throw new InvalidRequestException("Kosten müssen mindestens 0 sein");
        }
        if (mealDTO.stock() == null || mealDTO.stock() < 0) {
            throw new InvalidRequestException("Bestand muss mindestens 0 sein");
        }
        if (mealDTO.ingredients() == null || mealDTO.ingredients().trim().isEmpty()) {
            throw new InvalidRequestException("Zutaten sind erforderlich");
        }
        if (mealDTO.nutritionalInfo() == null) {
            throw new InvalidRequestException("Nährwertinformationen sind erforderlich");
        }
        if (mealDTO.categories() == null) {
            throw new InvalidRequestException("Kategorien sind erforderlich");
        }
        if (mealDTO.allergens() == null) {
            throw new InvalidRequestException("Allergene sind erforderlich");
        }
    }
    
    /**
     * Konvertierung von Entity zu DTO
     */
    private MealDTO convertToDTO(Meal meal) {
        MealDTO.NutritionalInfoDTO nutritionalInfoDTO = null;
        if (meal.getNutritionalInfo() != null) {
            nutritionalInfoDTO = new MealDTO.NutritionalInfoDTO(
                meal.getNutritionalInfo().getCalories(),
                meal.getNutritionalInfo().getProtein(),
                meal.getNutritionalInfo().getCarbs(),
                meal.getNutritionalInfo().getFat()
            );
        }
        
        return new MealDTO(
            meal.getId(),
            meal.getName(),
            meal.getDescription(),
            meal.getPrice(),
            meal.getCost(),
            meal.getStock(),
            meal.getIngredients(),
            nutritionalInfoDTO,
            meal.getCategories(),
            meal.getAllergens()
        );
    }
    
    /**
     * Konvertierung von DTO zu Entity
     */
    private Meal convertToEntity(MealDTO mealDTO) {
        NutritionalInfo nutritionalInfo = convertToNutritionalInfoEntity(mealDTO.nutritionalInfo());
        
        return new Meal(
            mealDTO.name(),
            mealDTO.description(),
            mealDTO.price(),
            mealDTO.cost(),
            mealDTO.stock(),
            mealDTO.ingredients(),
            nutritionalInfo,
            mealDTO.categories(),
            mealDTO.allergens()
        );
    }
    
    /**
     * Konvertierung von NutritionalInfoDTO zu NutritionalInfo Entity
     */
    private NutritionalInfo convertToNutritionalInfoEntity(MealDTO.NutritionalInfoDTO dto) {
        if (dto == null) {
            return null;
        }
        return new NutritionalInfo(
            dto.calories(),
            dto.protein(),
            dto.carbs(),
            dto.fat()
        );
    }
}
