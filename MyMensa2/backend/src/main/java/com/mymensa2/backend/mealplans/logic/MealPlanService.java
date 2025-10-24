package com.mymensa2.backend.mealplans.logic;

import com.mymensa2.backend.common.InvalidRequestException;
import com.mymensa2.backend.common.ResourceNotFoundException;
import com.mymensa2.backend.meals.dataaccess.Meal;
import com.mymensa2.backend.meals.facade.MealResponseDTO;
import com.mymensa2.backend.meals.logic.MealService;
import com.mymensa2.backend.mealplans.dataaccess.MealPlan;
import com.mymensa2.backend.mealplans.dataaccess.MealPlanId;
import com.mymensa2.backend.mealplans.dataaccess.MealPlanRepository;
import com.mymensa2.backend.mealplans.facade.MealPlanDayDTO;
import com.mymensa2.backend.mealplans.facade.MealPlanItemDTO;
import com.mymensa2.backend.mealplans.facade.MealPlanRequestDTO;
import com.mymensa2.backend.mealplans.facade.MealPlanResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MealPlanService {
    
    private final MealPlanRepository mealPlanRepository;
    private final MealService mealService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public MealPlanService(MealPlanRepository mealPlanRepository, MealService mealService) {
        this.mealPlanRepository = mealPlanRepository;
        this.mealService = mealService;
    }
    
    // Speiseplan für einen Zeitraum abrufen
    @Transactional(readOnly = true)
    public List<MealPlanDayDTO> getMealPlansForDateRange(String startDateStr, String endDateStr) {
        LocalDate startDate = parseDate(startDateStr);
        LocalDate endDate = parseDate(endDateStr);
        
        if (endDate.isBefore(startDate)) {
            throw new InvalidRequestException("Enddatum darf nicht vor Startdatum liegen");
        }
        
        List<MealPlan> mealPlans = mealPlanRepository.findByDateBetween(startDate, endDate);
        
        // Gruppiere nach Datum
        Map<LocalDate, List<MealPlan>> groupedByDate = mealPlans.stream()
                .collect(Collectors.groupingBy(MealPlan::getDate));
        
        // Erstelle Ergebnis-Liste für jeden Tag im Zeitraum
        List<MealPlanDayDTO> result = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            List<MealPlan> mealsForDay = groupedByDate.getOrDefault(currentDate, new ArrayList<>());
            
            List<MealPlanItemDTO> mealItems = mealsForDay.stream()
                    .map(mp -> new MealPlanItemDTO(convertMealToDTO(mp.getMeal()), mp.getStock()))
                    .collect(Collectors.toList());
            
            result.add(new MealPlanDayDTO(currentDate.format(DATE_FORMATTER), mealItems));
            currentDate = currentDate.plusDays(1);
        }
        
        return result;
    }
    
    // Gericht zum Speiseplan hinzufügen oder Bestand aktualisieren
    @Transactional
    public MealPlanResponseDTO addOrUpdateMealPlan(MealPlanRequestDTO request) {
        validateMealPlanRequest(request);
        
        Integer mealId = request.mealId();
        LocalDate date = parseDate(request.date());
        
        // Prüfe, ob Gericht existiert
        Meal meal = mealService.getMealByIdActive(mealId);
        
        // Prüfe, ob MealPlan bereits existiert
        MealPlan mealPlan = mealPlanRepository.findByMealIdAndDate(mealId, date)
                .orElse(new MealPlan(mealId, date, request.stock()));
        
        mealPlan.setStock(request.stock());
        MealPlan savedMealPlan = mealPlanRepository.save(mealPlan);
        
        return new MealPlanResponseDTO(
                savedMealPlan.getMealId(),
                savedMealPlan.getDate().format(DATE_FORMATTER),
                savedMealPlan.getStock(),
                convertMealToDTO(meal)
        );
    }
    
    // Gericht aus Speiseplan entfernen
    @Transactional
    public void removeMealFromPlan(Integer mealId, String dateStr) {
        LocalDate date = parseDate(dateStr);
        MealPlanId id = new MealPlanId(mealId, date);
        
        if (!mealPlanRepository.existsById(id)) {
            throw new ResourceNotFoundException("Gericht nicht im Speiseplan für " + dateStr + " gefunden");
        }
        
        mealPlanRepository.deleteById(id);
    }
    
    // Bestand reduzieren (bei Bestellung)
    @Transactional
    public void decreaseStock(Integer mealId, LocalDate date) {
        MealPlan mealPlan = mealPlanRepository.findByMealIdAndDate(mealId, date)
                .orElseThrow(() -> new ResourceNotFoundException("Gericht nicht im Speiseplan für diesen Tag gefunden"));
        
        if (mealPlan.getStock() <= 0) {
            throw new InvalidRequestException("Nicht genügend Bestand verfügbar");
        }
        
        mealPlan.setStock(mealPlan.getStock() - 1);
        mealPlanRepository.save(mealPlan);
    }
    
    // Prüfe Verfügbarkeit
    @Transactional(readOnly = true)
    public boolean isAvailable(Integer mealId, LocalDate date) {
        return mealPlanRepository.findByMealIdAndDate(mealId, date)
                .map(mp -> mp.getStock() > 0)
                .orElse(false);
    }
    
    // Hilfsmethode: Datum parsen
    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new InvalidRequestException("Ungültiges Datumsformat. Erwartet: yyyy-MM-dd");
        }
    }
    
    // Validierung der MealPlan-Request-Daten
    private void validateMealPlanRequest(MealPlanRequestDTO request) {
        if (request.mealId() == null) {
            throw new InvalidRequestException("Gericht-ID ist erforderlich");
        }
        if (request.date() == null || request.date().trim().isEmpty()) {
            throw new InvalidRequestException("Datum ist erforderlich");
        }
        if (request.stock() == null || request.stock() < 0) {
            throw new InvalidRequestException("Bestand muss 0 oder größer sein");
        }
    }
    
    // Konvertierung von Meal-Entity zu DTO
    private MealResponseDTO convertMealToDTO(Meal meal) {
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
