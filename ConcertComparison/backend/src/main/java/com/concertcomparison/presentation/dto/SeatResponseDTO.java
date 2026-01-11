package com.concertcomparison.presentation.dto;

/**
 * Response DTO für einzelnen Sitzplatz.
 * Entspricht OpenAPI-Schema "Seat".
 * 
 * Wird verwendet in der Seat-Verfügbarkeits-Antwort für ein Konzert.
 * Builder Pattern für saubere Object-Erstellung.
 */
public class SeatResponseDTO {
    
    private String id;          // String IDs gemäß OpenAPI
    private String block;
    private String category;
    private String row;
    private String number;
    private Double price;
    private String status;      // Enum-Name als String
    
    // Constructors
    public SeatResponseDTO() {}
    
    private SeatResponseDTO(Builder builder) {
        this.id = builder.id;
        this.block = builder.block;
        this.category = builder.category;
        this.row = builder.row;
        this.number = builder.number;
        this.price = builder.price;
        this.status = builder.status;
    }
    
    // Builder Pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String id;
        private String block;
        private String category;
        private String row;
        private String number;
        private Double price;
        private String status;
        
        public Builder id(String id) { this.id = id; return this; }
        public Builder block(String block) { this.block = block; return this; }
        public Builder category(String category) { this.category = category; return this; }
        public Builder row(String row) { this.row = row; return this; }
        public Builder number(String number) { this.number = number; return this; }
        public Builder price(Double price) { this.price = price; return this; }
        public Builder status(String status) { this.status = status; return this; }
        
        public SeatResponseDTO build() {
            return new SeatResponseDTO(this);
        }
    }
    
    // Getters
    public String getId() { return id; }
    public String getBlock() { return block; }
    public String getCategory() { return category; }
    public String getRow() { return row; }
    public String getNumber() { return number; }
    public Double getPrice() { return price; }
    public String getStatus() { return status; }
}
