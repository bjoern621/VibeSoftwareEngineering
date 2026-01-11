package com.concertcomparison.presentation.dto;

/**
 * DTO für aggregierte Verfügbarkeit einer Seat-Kategorie.
 * Entspricht OpenAPI-Schema "AvailabilityByCategory".
 */
public class AvailabilityByCategoryDTO {
    
    private String category;
    private Integer available;
    private Integer held;
    private Integer sold;
    
    // Constructors
    public AvailabilityByCategoryDTO() {}
    
    public AvailabilityByCategoryDTO(String category, Integer available, Integer held, Integer sold) {
        this.category = category;
        this.available = available;
        this.held = held;
        this.sold = sold;
    }
    
    // Getters & Setters
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public Integer getAvailable() {
        return available;
    }
    
    public void setAvailable(Integer available) {
        this.available = available;
    }
    
    public Integer getHeld() {
        return held;
    }
    
    public void setHeld(Integer held) {
        this.held = held;
    }
    
    public Integer getSold() {
        return sold;
    }
    
    public void setSold(Integer sold) {
        this.sold = sold;
    }
}
