package com.mymensa.backend.meals.dataaccess;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MealRepository extends JpaRepository<Meal, Integer> {  // Integer as per specification
    // Custom queries can be added here if needed
}
