package com.mymensa.backend.meals.logic;

import com.mymensa.backend.common.InvalidRequestException;
import com.mymensa.backend.common.ResourceNotFoundException;
import com.mymensa.backend.meals.dataaccess.Meal;
import com.mymensa.backend.meals.dataaccess.NutritionalInfo;
import com.mymensa.backend.meals.facade.MealDTO;
import com.mymensa.backend.meals.dataaccess.MealRepository;
import com.mymensa.backend.mealplans.dataaccess.MealPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MealService {
    
    @Autowired
    private MealRepository mealRepository;
    
    @Autowired
    private MealPlanRepository mealPlanRepository;
    
    /**
     * Alle nicht-gelöschten Gerichte abrufen
     */
    public List<MealDTO> getAllMeals() {
        return mealRepository.findAllActive()
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Gericht nach ID abrufen (nur nicht-gelöschte)
     */
    public MealDTO getMealById(Integer id) {  // Integer as per specification
        Meal meal = mealRepository.findByIdActive(id)
            .orElseThrow(() -> new ResourceNotFoundException("Gericht mit ID " + id + " nicht gefunden"));
        return convertToDTO(meal);
    }
    
    /**
     * Gericht nach ID abrufen (auch gelöschte - für historische Daten wie Orders)
     */
    public MealDTO getMealByIdIncludingDeleted(Integer id) {
        Meal meal = mealRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Gericht mit ID " + id + " nicht gefunden"));
        return convertToDTO(meal);
    }
    
    /**
     * Neues Gericht erstellen
     */
    public MealDTO createMeal(MealDTO mealDTO) {
        // Bei POST darf keine ID mitgesendet werden
        if (mealDTO.id() != null) {
            throw new InvalidRequestException("Bei der Erstellung eines neuen Gerichts darf keine ID angegeben werden");
        }
        
        validateMealDTO(mealDTO);
        Meal meal = convertToEntity(mealDTO);
        Meal savedMeal = mealRepository.save(meal);
        return convertToDTO(savedMeal);
    }
    
    /**
     * Gericht aktualisieren
     */
    public MealDTO updateMeal(Integer id, MealDTO mealDTO) {  // Integer as per specification
        Meal existingMeal = mealRepository.findByIdActive(id)
            .orElseThrow(() -> new ResourceNotFoundException("Gericht mit ID " + id + " nicht gefunden"));
        
        validateMealDTO(mealDTO);
        
        // Update fields - Records use accessor methods without 'get' prefix
        existingMeal.setName(mealDTO.name());
        existingMeal.setDescription(mealDTO.description());
        existingMeal.setPrice(mealDTO.price());
        existingMeal.setCost(mealDTO.cost());
        existingMeal.setIngredients(mealDTO.ingredients());
        existingMeal.setNutritionalInfo(convertToNutritionalInfoEntity(mealDTO.nutritionalInfo()));
        existingMeal.setCategories(mealDTO.categories());
        existingMeal.setAllergens(mealDTO.allergens());
        
        Meal updatedMeal = mealRepository.save(existingMeal);
        return convertToDTO(updatedMeal);
    }
    
    /**
     * Gericht löschen (Soft Delete)
     * - Markiert das Meal als gelöscht
     * - Entfernt es aus zukünftigen MealPlans
     * - Behält historische Daten (Orders, vergangene MealPlans)
     */
    @Transactional
    public void deleteMeal(Integer id) {  // Integer as per specification
        Meal meal = mealRepository.findByIdActive(id)
            .orElseThrow(() -> new ResourceNotFoundException("Gericht mit ID " + id + " nicht gefunden"));
        
        // Soft Delete: Meal als gelöscht markieren
        meal.setDeleted(true);
        meal.setDeletedAt(LocalDateTime.now());
        mealRepository.save(meal);
        
        // Entferne Meal aus zukünftigen MealPlans (ab heute)
        LocalDate today = LocalDate.now();
        mealPlanRepository.deleteByMealIdAndDateGreaterThanEqual(id, today);
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
