package com.concertcomparison.presentation.dto;

import com.concertcomparison.domain.model.OrderStatus;

import java.time.LocalDateTime;

/**
 * Response DTO für Order (US-03).
 * 
 * Wird nach erfolgreichem Ticket-Kauf zurückgegeben.
 */
public class OrderResponseDTO {

    private Long orderId;
    private Long seatId;
    private String userId;
    private Double totalPrice;
    private OrderStatus status;
    private LocalDateTime purchaseDate;
    private String paymentStatus;

    // ==================== CONSTRUCTORS ====================

    public OrderResponseDTO() {
        // Default Constructor für Jackson
    }

    public OrderResponseDTO(
            Long orderId,
            Long seatId,
            String userId,
            Double totalPrice,
            OrderStatus status,
            LocalDateTime purchaseDate,
            String paymentStatus) {
        this.orderId = orderId;
        this.seatId = seatId;
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.status = status;
        this.purchaseDate = purchaseDate;
        this.paymentStatus = paymentStatus;
    }

    // ==================== GETTERS & SETTERS ====================

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    @Override
    public String toString() {
        return "OrderResponseDTO{" +
                "orderId=" + orderId +
                ", seatId=" + seatId +
                ", userId='" + userId + '\'' +
                ", totalPrice=" + totalPrice +
                ", status=" + status +
                ", purchaseDate=" + purchaseDate +
                ", paymentStatus='" + paymentStatus + '\'' +
                '}';
    }
}
