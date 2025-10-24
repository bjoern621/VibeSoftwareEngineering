package com.mymensa2.backend.meals.dataaccess;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "meals")
public class Meal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, length = 1000)
    private String description;
    
    @Column(nullable = false)
    private Float price;
    
    @Column(nullable = false)
    private Float cost;
    
    @Column(nullable = false, length = 2000)
    private String ingredients;
    
    @Embedded
    private NutritionalInfo nutritionalInfo;
    
    @ElementCollection
    @CollectionTable(name = "meal_categories", joinColumns = @JoinColumn(name = "meal_id"))
    @Column(name = "category")
    private List<String> categories;
    
    @ElementCollection
    @CollectionTable(name = "meal_allergens", joinColumns = @JoinColumn(name = "meal_id"))
    @Column(name = "allergen")
    private List<String> allergens;
    
    // Soft Delete Fields
    @Column(nullable = false)
    private Boolean deleted = false;
    
    @Column
    private LocalDateTime deletedAt;
    
    // Constructors
    public Meal() {
    }
    
    public Meal(String name, String description, Float price, Float cost, 
                String ingredients, NutritionalInfo nutritionalInfo, List<String> categories, 
                List<String> allergens) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.cost = cost;
        this.ingredients = ingredients;
        this.nutritionalInfo = nutritionalInfo;
        this.categories = categories;
        this.allergens = allergens;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Float getPrice() {
        return price;
    }
    
    public void setPrice(Float price) {
        this.price = price;
    }
    
    public Float getCost() {
        return cost;
    }
    
    public void setCost(Float cost) {
        this.cost = cost;
    }
    
    public String getIngredients() {
        return ingredients;
    }
    
    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }
    
    public NutritionalInfo getNutritionalInfo() {
        return nutritionalInfo;
    }
    
    public void setNutritionalInfo(NutritionalInfo nutritionalInfo) {
        this.nutritionalInfo = nutritionalInfo;
    }
    
    public List<String> getCategories() {
        return categories;
    }
    
    public void setCategories(List<String> categories) {
        this.categories = categories;
    }
    
    public List<String> getAllergens() {
        return allergens;
    }
    
    public void setAllergens(List<String> allergens) {
        this.allergens = allergens;
    }
    
    public Boolean getDeleted() {
        return deleted;
    }
    
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
    
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
    
    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
