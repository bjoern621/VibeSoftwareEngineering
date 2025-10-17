package com.mymensa.backend.mealplans.dataaccess;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Composite Key für MealPlan Entity
 */
public class MealPlanId implements Serializable {
    
    private Integer mealId;
    private LocalDate date;
    
    // Constructors
    public MealPlanId() {
    }
    
    public MealPlanId(Integer mealId, LocalDate date) {
        this.mealId = mealId;
        this.date = date;
    }
    
    // Getters and Setters
    public Integer getMealId() {
        return mealId;
    }
    
    public void setMealId(Integer mealId) {
        this.mealId = mealId;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MealPlanId that = (MealPlanId) o;
        return Objects.equals(mealId, that.mealId) && 
               Objects.equals(date, that.date);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(mealId, date);
    }
}
