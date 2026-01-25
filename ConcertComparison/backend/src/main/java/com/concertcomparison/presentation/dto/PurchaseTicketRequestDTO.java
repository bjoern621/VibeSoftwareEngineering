package com.concertcomparison.presentation.dto;

import com.concertcomparison.domain.model.PaymentMethod;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO für Ticket-Kauf (US-03).
 * 
 * POST /api/orders
 * Body: { "holdId": 123, "userId": "user@example.com", "paymentMethod": "CREDIT_CARD" }
 */
public class PurchaseTicketRequestDTO {

    @NotNull(message = "HoldId darf nicht null sein")
    private Long holdId;

    @NotNull(message = "UserId darf nicht null sein")
    private String userId;
    
    private PaymentMethod paymentMethod;  // Optional, Default: CREDIT_CARD

    // ==================== CONSTRUCTORS ====================

    public PurchaseTicketRequestDTO() {
        // Default Constructor für Jackson
    }

    public PurchaseTicketRequestDTO(Long holdId, String userId) {
        this.holdId = holdId;
        this.userId = userId;
        this.paymentMethod = PaymentMethod.CREDIT_CARD;  // Default
    }
    
    public PurchaseTicketRequestDTO(Long holdId, String userId, PaymentMethod paymentMethod) {
        this.holdId = holdId;
        this.userId = userId;
        this.paymentMethod = paymentMethod;
    }

    // ==================== GETTERS & SETTERS ====================

    public Long getHoldId() {
        return holdId;
    }

    public void setHoldId(Long holdId) {
        this.holdId = holdId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    @Override
    public String toString() {
        return "PurchaseTicketRequestDTO{" +
                "holdId=" + holdId +
                ", userId='" + userId + '\'' +
                '}';
    }
}
