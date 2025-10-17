package com.mymensa.backend.mealplans.dataaccess;

import com.mymensa.backend.meals.dataaccess.Meal;
import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * MealPlan Entity - Repräsentiert die Verbindung zwischen Meal und Datum mit Bestand
 * Composite Key: mealId + date
 */
@Entity
@Table(name = "meal_plans")
@IdClass(MealPlanId.class)
public class MealPlan {
    
    @Id
    @Column(name = "meal_id", nullable = false)
    private Integer mealId;  // int as per specification
    
    @Id
    @Column(nullable = false)
    private LocalDate date;  // date in format YYYY-MM-DD
    
    @ManyToOne
    @JoinColumn(name = "meal_id", insertable = false, updatable = false)
    private Meal meal;
    
    @Column(nullable = false)
    private Integer stock;  // int as per specification - Verfügbare Portionen
    
    // Constructors
    public MealPlan() {
    }
    
    public MealPlan(Integer mealId, LocalDate date, Integer stock) {
        this.mealId = mealId;
        this.date = date;
        this.stock = stock;
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
    
    public Meal getMeal() {
        return meal;
    }
    
    public void setMeal(Meal meal) {
        this.meal = meal;
    }
    
    public Integer getStock() {
        return stock;
    }
    
    public void setStock(Integer stock) {
        this.stock = stock;
    }
}
