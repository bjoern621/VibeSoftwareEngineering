package com.mymensa2.backend.mealplans.logic;

import com.mymensa2.backend.common.InvalidRequestException;
import com.mymensa2.backend.common.ResourceNotFoundException;
import com.mymensa2.backend.meals.dataaccess.MealRepository;
import com.mymensa2.backend.meals.facade.MealDTO;
import com.mymensa2.backend.meals.logic.MealService;
import com.mymensa2.backend.mealplans.dataaccess.MealPlan;
import com.mymensa2.backend.mealplans.dataaccess.MealPlanRepository;
import com.mymensa2.backend.mealplans.facade.MealPlanDayDTO;
import com.mymensa2.backend.mealplans.facade.MealPlanEntryDTO;
import com.mymensa2.backend.mealplans.facade.MealPlanRequestDTO;
import com.mymensa2.backend.mealplans.facade.MealPlanResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
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
    
    @Autowired
    private MealPlanRepository mealPlanRepository;
    
    @Autowired
    private MealRepository mealRepository;
    
    @Autowired
    private MealService mealService;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * Speiseplan für einen Zeitraum abrufen
     * GET /api/meal-plans?startDate={date}&endDate={date}
     */
    @Transactional(readOnly = true)
    public List<MealPlanDayDTO> getMealPlansForDateRange(String startDateStr, String endDateStr) {
        // Validierung und Parsing der Daten
        LocalDate startDate = parseDate(startDateStr);
        LocalDate endDate = parseDate(endDateStr);
        
        if (endDate.isBefore(startDate)) {
            throw new InvalidRequestException("endDate darf nicht vor startDate liegen");
        }
        
        // Alle MealPlans für den Zeitraum laden
        List<MealPlan> mealPlans = mealPlanRepository.findByDateBetween(startDate, endDate);
        
        // Nach Datum gruppieren
        Map<LocalDate, List<MealPlan>> mealPlansByDate = mealPlans.stream()
            .collect(Collectors.groupingBy(MealPlan::getDate));
        
        // Für jeden Tag im Zeitraum ein MealPlanDayDTO erstellen
        List<MealPlanDayDTO> result = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            List<MealPlan> mealsForDay = mealPlansByDate.getOrDefault(currentDate, new ArrayList<>());
            
            List<MealPlanEntryDTO> entries = mealsForDay.stream()
                .map(mealPlan -> {
                    // Auch gelöschte Meals anzeigen für historische Speisepläne
                    MealDTO mealDTO = mealService.getMealByIdIncludingDeleted(mealPlan.getMealId());
                    return new MealPlanEntryDTO(mealDTO, mealPlan.getStock());
                })
                .collect(Collectors.toList());
            
            result.add(new MealPlanDayDTO(
                currentDate.format(DATE_FORMATTER),
                entries
            ));
            
            currentDate = currentDate.plusDays(1);
        }
        
        return result;
    }
    
    /**
     * Gericht zum Speiseplan hinzufügen oder Bestand aktualisieren
     * PUT /api/meal-plans
     * @return MealPlanResult mit Response und created-Flag
     */
    @Transactional
    public MealPlanResult addOrUpdateMealPlan(MealPlanRequestDTO requestDTO) {
        // Validierung
        validateMealPlanRequest(requestDTO);
        
        LocalDate date = parseDate(requestDTO.date());
        
        // Prüfen ob Meal existiert UND nicht gelöscht ist
        mealRepository.findByIdAndDeletedFalse(requestDTO.mealId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Gericht mit ID " + requestDTO.mealId() + " nicht gefunden oder wurde gelöscht"
            ));
        
        // Prüfen ob MealPlan bereits existiert
        boolean isNew = !mealPlanRepository
            .findByMealIdAndDate(requestDTO.mealId(), date)
            .isPresent();
        
        MealPlan mealPlan = mealPlanRepository
            .findByMealIdAndDate(requestDTO.mealId(), date)
            .orElse(new MealPlan(requestDTO.mealId(), date, requestDTO.stock()));
        
        // Stock aktualisieren
        mealPlan.setStock(requestDTO.stock());
        
        // Speichern
        mealPlanRepository.save(mealPlan);
        
        // Response erstellen
        MealDTO mealDTO = mealService.getMealById(requestDTO.mealId());
        MealPlanResponseDTO responseDTO = new MealPlanResponseDTO(
            requestDTO.mealId(),
            requestDTO.date(),
            requestDTO.stock(),
            mealDTO
        );
        
        return new MealPlanResult(responseDTO, isNew);
    }
    
    /**
     * Gericht aus Speiseplan entfernen
     * DELETE /api/meal-plans?mealId={mealId}&date={date}
     */
    @Transactional
    public void removeMealFromPlan(Integer mealId, String dateStr) {
        LocalDate date = parseDate(dateStr);
        
        // Prüfen ob MealPlan existiert
        mealPlanRepository.findByMealIdAndDate(mealId, date)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Gericht nicht im Speiseplan für diesen Tag gefunden"
            ));
        
        mealPlanRepository.deleteByMealIdAndDate(mealId, date);
    }
    
    /**
     * Validierung der MealPlanRequestDTO
     */
    private void validateMealPlanRequest(MealPlanRequestDTO requestDTO) {
        if (requestDTO.mealId() == null) {
            throw new InvalidRequestException("mealId ist erforderlich");
        }
        if (requestDTO.date() == null || requestDTO.date().trim().isEmpty()) {
            throw new InvalidRequestException("date ist erforderlich");
        }
        if (requestDTO.stock() == null || requestDTO.stock() < 0) {
            throw new InvalidRequestException("stock muss mindestens 0 sein");
        }
    }
    
    /**
     * Hilfsmethode zum Parsen eines Datums
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new InvalidRequestException("Datum ist erforderlich");
        }
        
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new InvalidRequestException(
                "Ungültiges Datumsformat. Erwartet: YYYY-MM-DD"
            );
        }
    }
}
