package com.mymensa.backend.orders.dataaccess;

import com.mymensa.backend.meals.dataaccess.Meal;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne
    @JoinColumn(name = "meal_id", nullable = false)
    private Meal meal;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(nullable = false)
    private Boolean paid = false;
    
    @Column(unique = true)
    private String qrCode;
    
    @Column
    private Boolean collected = false;
    
    @Column
    private LocalDateTime collectedAt;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public Order() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Order(Meal meal, LocalDate date) {
        this.meal = meal;
        this.date = date;
        this.paid = false;
        this.collected = false;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Meal getMeal() {
        return meal;
    }
    
    public void setMeal(Meal meal) {
        this.meal = meal;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public Boolean getPaid() {
        return paid;
    }
    
    public void setPaid(Boolean paid) {
        this.paid = paid;
    }
    
    public String getQrCode() {
        return qrCode;
    }
    
    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }
    
    public Boolean getCollected() {
        return collected;
    }
    
    public void setCollected(Boolean collected) {
        this.collected = collected;
    }
    
    public LocalDateTime getCollectedAt() {
        return collectedAt;
    }
    
    public void setCollectedAt(LocalDateTime collectedAt) {
        this.collectedAt = collectedAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
