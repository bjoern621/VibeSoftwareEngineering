package com.mymensa.backend.mealplans.dataaccess;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MealPlanRepository extends JpaRepository<MealPlan, MealPlanId> {
    
    /**
     * Findet alle MealPlans für einen bestimmten Datumsbereich
     */
    List<MealPlan> findByDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Findet einen MealPlan für ein bestimmtes Gericht an einem bestimmten Datum
     */
    Optional<MealPlan> findByMealIdAndDate(Integer mealId, LocalDate date);
    
    /**
     * Löscht einen MealPlan für ein bestimmtes Gericht an einem bestimmten Datum
     */
    void deleteByMealIdAndDate(Integer mealId, LocalDate date);
    
    /**
     * Löscht alle MealPlans für ein Gericht ab einem bestimmten Datum (inklusive)
     * Wird für Soft Delete von Meals verwendet
     */
    void deleteByMealIdAndDateGreaterThanEqual(Integer mealId, LocalDate date);
}
