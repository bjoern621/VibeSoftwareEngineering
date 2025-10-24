package com.mymensa2.backend.orders.dataaccess;

import com.mymensa2.backend.meals.dataaccess.Meal;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false)
    private Integer mealId;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mealId", insertable = false, updatable = false)
    private Meal meal;
    
    @Column(nullable = false)
    private LocalDateTime orderDate;
    
    @Column(nullable = false)
    private LocalDate pickupDate;
    
    @Column(nullable = false)
    private Boolean paid = false;
    
    @Column
    private LocalDateTime paidAt;
    
    @Column
    private String paymentMethod;
    
    @Column
    private String paymentTransactionId;
    
    @Column(unique = true)
    private String qrCode;
    
    @Column(nullable = false)
    private Boolean collected = false;
    
    @Column
    private LocalDateTime collectedAt;
    
    // Constructors
    public Order() {
    }
    
    public Order(Integer mealId, LocalDate pickupDate) {
        this.mealId = mealId;
        this.orderDate = LocalDateTime.now();
        this.pickupDate = pickupDate;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getMealId() {
        return mealId;
    }
    
    public void setMealId(Integer mealId) {
        this.mealId = mealId;
    }
    
    public Meal getMeal() {
        return meal;
    }
    
    public void setMeal(Meal meal) {
        this.meal = meal;
    }
    
    public LocalDateTime getOrderDate() {
        return orderDate;
    }
    
    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }
    
    public LocalDate getPickupDate() {
        return pickupDate;
    }
    
    public void setPickupDate(LocalDate pickupDate) {
        this.pickupDate = pickupDate;
    }
    
    public Boolean getPaid() {
        return paid;
    }
    
    public void setPaid(Boolean paid) {
        this.paid = paid;
    }
    
    public LocalDateTime getPaidAt() {
        return paidAt;
    }
    
    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getPaymentTransactionId() {
        return paymentTransactionId;
    }
    
    public void setPaymentTransactionId(String paymentTransactionId) {
        this.paymentTransactionId = paymentTransactionId;
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
}
