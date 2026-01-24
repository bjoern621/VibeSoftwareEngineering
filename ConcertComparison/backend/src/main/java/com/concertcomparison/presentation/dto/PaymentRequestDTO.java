package com.concertcomparison.presentation.dto;

import com.concertcomparison.domain.model.PaymentMethod;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO für Payment-Processing.
 * 
 * POST /api/payments/process
 */
public class PaymentRequestDTO {
    
    @NotNull(message = "OrderId darf nicht null sein")
    private Long orderId;
    
    @NotNull(message = "PaymentMethod darf nicht null sein")
    private PaymentMethod paymentMethod;
    
    // ==================== CONSTRUCTORS ====================
    
    public PaymentRequestDTO() {
    }
    
    public PaymentRequestDTO(Long orderId, PaymentMethod paymentMethod) {
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
    }
    
    // ==================== GETTERS & SETTERS ====================
    
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
