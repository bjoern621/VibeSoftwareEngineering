package com.mymensa2.backend.mealplans.dataaccess;

import com.mymensa2.backend.meals.dataaccess.Meal;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "meal_plans")
@IdClass(MealPlanId.class)
public class MealPlan {
    
    @Id
    @Column(name = "meal_id")
    private Integer mealId;
    
    @Id
    @Column(name = "date")
    private LocalDate date;
    
    @Column(nullable = false)
    private Integer stock;
    
    @ManyToOne
    @JoinColumn(name = "meal_id", insertable = false, updatable = false)
    private Meal meal;
    
    public MealPlan() {
    }
    
    public MealPlan(Integer mealId, LocalDate date, Integer stock) {
        this.mealId = mealId;
        this.date = date;
        this.stock = stock;
    }
    
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
    
    public Integer getStock() {
        return stock;
    }
    
    public void setStock(Integer stock) {
        this.stock = stock;
    }
    
    public Meal getMeal() {
        return meal;
    }
    
    public void setMeal(Meal meal) {
        this.meal = meal;
    }
}

class MealPlanId implements Serializable {
    private Integer mealId;
    private LocalDate date;
    
    public MealPlanId() {
    }
    
    public MealPlanId(Integer mealId, LocalDate date) {
        this.mealId = mealId;
        this.date = date;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MealPlanId that = (MealPlanId) o;
        return Objects.equals(mealId, that.mealId) && Objects.equals(date, that.date);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(mealId, date);
    }
}
