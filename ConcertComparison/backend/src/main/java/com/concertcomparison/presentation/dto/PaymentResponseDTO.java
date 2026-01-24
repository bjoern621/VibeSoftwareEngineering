package com.concertcomparison.presentation.dto;

import com.concertcomparison.domain.model.PaymentStatus;

/**
 * Response DTO für Payment-Processing.
 * 
 * POST /api/payments/process Response
 * GET /api/payments/status/{orderId} Response
 */
public class PaymentResponseDTO {
    
    private Long orderId;
    private PaymentStatus status;
    private String transactionId;
    private String message;
    private Long estimatedProcessingTimeMs;
    
    // ==================== CONSTRUCTORS ====================
    
    public PaymentResponseDTO() {
    }
    
    public PaymentResponseDTO(Long orderId, PaymentStatus status, String message) {
        this.orderId = orderId;
        this.status = status;
        this.message = message;
    }
    
    public PaymentResponseDTO(Long orderId, PaymentStatus status, String transactionId, String message) {
        this.orderId = orderId;
        this.status = status;
        this.transactionId = transactionId;
        this.message = message;
    }
    
    // ==================== FACTORY METHODS ====================
    
    /**
     * Factory: Payment wird asynchron prozessiert.
     */
    public static PaymentResponseDTO processing(Long orderId) {
        PaymentResponseDTO dto = new PaymentResponseDTO(
            orderId,
            PaymentStatus.PENDING,
            "Payment wird verarbeitet"
        );
        dto.setEstimatedProcessingTimeMs(2000L); // 1-3s Durchschnitt
        return dto;
    }
    
    /**
     * Factory: Payment erfolgreich abgeschlossen.
     */
    public static PaymentResponseDTO success(Long orderId, String transactionId) {
        return new PaymentResponseDTO(
            orderId,
            PaymentStatus.COMPLETED,
            transactionId,
            "Zahlung erfolgreich abgeschlossen"
        );
    }
    
    /**
     * Factory: Payment fehlgeschlagen.
     */
    public static PaymentResponseDTO failed(Long orderId, String errorMessage) {
        return new PaymentResponseDTO(
            orderId,
            PaymentStatus.FAILED,
            "Zahlung fehlgeschlagen: " + errorMessage
        );
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
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Long getEstimatedProcessingTimeMs() {
        return estimatedProcessingTimeMs;
    }
    
    public void setEstimatedProcessingTimeMs(Long estimatedProcessingTimeMs) {
        this.estimatedProcessingTimeMs = estimatedProcessingTimeMs;
    }
}
