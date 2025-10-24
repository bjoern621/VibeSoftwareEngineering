package com.mymensa2.backend.meals.dataaccess;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MealRepository extends JpaRepository<Meal, Integer> {
    
    // Findet alle aktiven (nicht gelöschten) Gerichte
    @Query("SELECT m FROM Meal m WHERE m.deleted = false")
    List<Meal> findAllActive();
    
    // Findet ein Gericht nach ID, nur wenn es nicht gelöscht ist
    @Query("SELECT m FROM Meal m WHERE m.id = :id AND m.deleted = false")
    Optional<Meal> findByIdActive(Integer id);
}
