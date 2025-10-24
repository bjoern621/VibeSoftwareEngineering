package com.mymensa2.backend.mealplans.dataaccess;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MealPlanRepository extends JpaRepository<MealPlan, MealPlanId> {
    
    // Findet alle Speisepläne in einem Zeitraum
    @Query("SELECT mp FROM MealPlan mp WHERE mp.date BETWEEN :startDate AND :endDate ORDER BY mp.date")
    List<MealPlan> findByDateBetween(LocalDate startDate, LocalDate endDate);
    
    // Findet Speiseplan für ein Gericht an einem bestimmten Tag
    Optional<MealPlan> findByMealIdAndDate(Integer mealId, LocalDate date);
}
