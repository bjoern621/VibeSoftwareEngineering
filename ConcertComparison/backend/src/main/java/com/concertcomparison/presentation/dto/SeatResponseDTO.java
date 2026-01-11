package com.concertcomparison.presentation.dto;

import java.time.LocalDateTime;

/**
 * Response DTO für einzelnen Sitzplatz.
 * 
 * Transportiert Seat-Informationen von der API zum Frontend.
 * Kapselt Domain Entity und exponiert nur relevante Daten.
 */
public class SeatResponseDTO {
    
    private Long id;
    private String seatNumber;
    private String category;
    private String block;
    private String status;
    private String statusDisplayName;
    private boolean isAvailable;
    private LocalDateTime holdExpiresAt;
    
    // ==================== CONSTRUCTORS ====================
    
    public SeatResponseDTO() {
        // Default Constructor für Jackson Deserialization
    }
    
    private SeatResponseDTO(Builder builder) {
        this.id = builder.id;
        this.seatNumber = builder.seatNumber;
        this.category = builder.category;
        this.block = builder.block;
        this.status = builder.status;
        this.statusDisplayName = builder.statusDisplayName;
        this.isAvailable = builder.isAvailable;
        this.holdExpiresAt = builder.holdExpiresAt;
    }
    
    // ==================== BUILDER PATTERN ====================
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Long id;
        private String seatNumber;
        private String category;
        private String block;
        private String status;
        private String statusDisplayName;
        private boolean isAvailable;
        private LocalDateTime holdExpiresAt;
        
        public Builder id(Long id) {
            this.id = id;
            return this;
        }
        
        public Builder seatNumber(String seatNumber) {
            this.seatNumber = seatNumber;
            return this;
        }
        
        public Builder category(String category) {
            this.category = category;
            return this;
        }
        
        public Builder block(String block) {
            this.block = block;
            return this;
        }
        
        public Builder status(String status) {
            this.status = status;
            return this;
        }
        
        public Builder statusDisplayName(String statusDisplayName) {
            this.statusDisplayName = statusDisplayName;
            return this;
        }
        
        public Builder isAvailable(boolean isAvailable) {
            this.isAvailable = isAvailable;
            return this;
        }
        
        public Builder holdExpiresAt(LocalDateTime holdExpiresAt) {
            this.holdExpiresAt = holdExpiresAt;
            return this;
        }
        
        public SeatResponseDTO build() {
            return new SeatResponseDTO(this);
        }
    }
    
    // ==================== GETTERS & SETTERS ====================
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getSeatNumber() {
        return seatNumber;
    }
    
    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getBlock() {
        return block;
    }
    
    public void setBlock(String block) {
        this.block = block;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getStatusDisplayName() {
        return statusDisplayName;
    }
    
    public void setStatusDisplayName(String statusDisplayName) {
        this.statusDisplayName = statusDisplayName;
    }
    
    public boolean isAvailable() {
        return isAvailable;
    }
    
    public void setAvailable(boolean available) {
        isAvailable = available;
    }
    
    public LocalDateTime getHoldExpiresAt() {
        return holdExpiresAt;
    }
    
    public void setHoldExpiresAt(LocalDateTime holdExpiresAt) {
        this.holdExpiresAt = holdExpiresAt;
    }
}
