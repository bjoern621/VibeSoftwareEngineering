package com.concertcomparison.presentation.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO für Ticket-Kauf (US-03).
 * 
 * POST /api/orders
 * Body: { "holdId": 123, "userId": "user@example.com" }
 */
public class PurchaseTicketRequestDTO {

    @NotNull(message = "HoldId darf nicht null sein")
    private Long holdId;

    @NotNull(message = "UserId darf nicht null sein")
    private String userId;

    // ==================== CONSTRUCTORS ====================

    public PurchaseTicketRequestDTO() {
        // Default Constructor für Jackson
    }

    public PurchaseTicketRequestDTO(Long holdId, String userId) {
        this.holdId = holdId;
        this.userId = userId;
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

    @Override
    public String toString() {
        return "PurchaseTicketRequestDTO{" +
                "holdId=" + holdId +
                ", userId='" + userId + '\'' +
                '}';
    }
}
