package com.concertcomparison.presentation.dto;

import com.concertcomparison.domain.model.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Webhook DTO für asynchrone Payment-Benachrichtigungen.
 * 
 * POST /api/payments/webhook
 * 
 * Simuliert externe Webhook-Notification von Payment-Provider.
 */
public class WebhookPaymentDTO {
    
    @NotNull(message = "OrderId darf nicht null sein")
    private Long orderId;
    
    @NotNull(message = "Status darf nicht null sein")
    private PaymentStatus status;
    
    @NotBlank(message = "TransactionId darf nicht leer sein")
    private String transactionId;
    
    private String errorMessage;
    
    // ==================== CONSTRUCTORS ====================
    
    public WebhookPaymentDTO() {
    }
    
    public WebhookPaymentDTO(Long orderId, PaymentStatus status, String transactionId) {
        this.orderId = orderId;
        this.status = status;
        this.transactionId = transactionId;
    }
    
    public WebhookPaymentDTO(Long orderId, PaymentStatus status, String transactionId, String errorMessage) {
        this.orderId = orderId;
        this.status = status;
        this.transactionId = transactionId;
        this.errorMessage = errorMessage;
    }
    
    // ==================== GETTERS & SETTERS ====================
    
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
    public PaymentStatus getStatus() {
        return status;
    }
    
    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
