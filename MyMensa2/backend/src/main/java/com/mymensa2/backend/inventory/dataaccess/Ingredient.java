package com.mymensa2.backend.inventory.dataaccess;

import jakarta.persistence.*;

@Entity
@Table(name = "ingredients")
public class Ingredient {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String unit;
    
    @Column(nullable = false)
    private Float stockQuantity;
    
    @Column(nullable = false)
    private Float minStockLevel;
    
    @Column(nullable = false)
    private Float pricePerUnit;
    
    @Column(nullable = false)
    private String supplierId;
    
    // Constructors
    public Ingredient() {
    }
    
    public Ingredient(String name, String unit, Float stockQuantity, Float minStockLevel, Float pricePerUnit, String supplierId) {
        this.name = name;
        this.unit = unit;
        this.stockQuantity = stockQuantity;
        this.minStockLevel = minStockLevel;
        this.pricePerUnit = pricePerUnit;
        this.supplierId = supplierId;
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
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    public Float getStockQuantity() {
        return stockQuantity;
    }
    
    public void setStockQuantity(Float stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
    
    public Float getMinStockLevel() {
        return minStockLevel;
    }
    
    public void setMinStockLevel(Float minStockLevel) {
        this.minStockLevel = minStockLevel;
    }
    
    public Float getPricePerUnit() {
        return pricePerUnit;
    }
    
    public void setPricePerUnit(Float pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }
    
    public String getSupplierId() {
        return supplierId;
    }
    
    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }
    
    public boolean needsReorder() {
        return stockQuantity < minStockLevel;
    }
}
