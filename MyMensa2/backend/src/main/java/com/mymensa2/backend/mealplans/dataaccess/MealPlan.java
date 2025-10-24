package com.mymensa2.backend.mealplans.dataaccess;

import com.mymensa2.backend.meals.dataaccess.Meal;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "meal_plans")
@IdClass(MealPlanId.class)
public class MealPlan {
    
    @Id
    private Integer mealId;
    
    @Id
    private LocalDate date;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mealId", insertable = false, updatable = false)
    private Meal meal;
    
    @Column(nullable = false)
    private Integer stock;
    
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
