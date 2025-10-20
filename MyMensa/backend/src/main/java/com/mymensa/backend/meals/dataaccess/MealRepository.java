package com.mymensa.backend.meals.dataaccess;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MealRepository extends JpaRepository<Meal, Integer> {  // Integer as per specification
    
    /**
     * Findet alle nicht-gelöschten Meals
     */
    @Query("SELECT m FROM Meal m WHERE m.deleted = false")
    List<Meal> findAllActive();
    
    /**
     * Findet ein nicht-gelöschtes Meal anhand der ID
     */
    @Query("SELECT m FROM Meal m WHERE m.id = :id AND m.deleted = false")
    Optional<Meal> findByIdActive(Integer id);
}
