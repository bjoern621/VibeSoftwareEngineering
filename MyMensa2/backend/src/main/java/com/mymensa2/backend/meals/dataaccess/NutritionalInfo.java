package com.mymensa2.backend.meals.dataaccess;

import jakarta.persistence.Embeddable;

@Embeddable
public class NutritionalInfo {
    
    private Integer calories;
    private Float protein;
    private Float carbs;
    private Float fat;
    
    public NutritionalInfo() {
    }
    
    public NutritionalInfo(Integer calories, Float protein, Float carbs, Float fat) {
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
    }
    
    public Integer getCalories() {
        return calories;
    }
    
    public void setCalories(Integer calories) {
        this.calories = calories;
    }
    
    public Float getProtein() {
        return protein;
    }
    
    public void setProtein(Float protein) {
        this.protein = protein;
    }
    
    public Float getCarbs() {
        return carbs;
    }
    
    public void setCarbs(Float carbs) {
        this.carbs = carbs;
    }
    
    public Float getFat() {
        return fat;
    }
    
    public void setFat(Float fat) {
        this.fat = fat;
    }
}
