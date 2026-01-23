package com.concertcomparison.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO für einzelnen Sitzplatz-Erstellung (in Bulk).
 * 
 * Wird verwendet in CreateSeatsRequestDTO als Array.
 * 
 * Validierung:
 * - seatNumber: Eindeutiger Identifier (z.B. "A-12")
 * - category: Kategorie (z.B. "VIP", "CATEGORY_A")
 * - block: Bereich (z.B. "Block A")
 * - row: Reihe (z.B. "A")
 * - number: Sitznummer (z.B. "12")
 * - price: Muss positiv sein
 */
@Schema(description = "Request für einen Sitzplatz (in Bulk-Operationen)")
public class CreateSeatRequestDTO {
    
    @NotBlank(message = "Seat number is required")
    @Schema(
        description = "Eindeutige Sitzplatz-Nummer (z.B. A-12)",
        example = "A-12",
        maxLength = 20
    )
    private String seatNumber;
    
    @NotBlank(message = "Category is required")
    @Schema(
        description = "Preiskategorie (z.B. VIP, CATEGORY_A)",
        example = "VIP",
        maxLength = 50
    )
    private String category;
    
    @NotBlank(message = "Block is required")
    @Schema(
        description = "Bereich/Block (z.B. Block A)",
        example = "Block A",
        maxLength = 50
    )
    private String block;
    
    @NotBlank(message = "Row is required")
    @Schema(
        description = "Reihe (z.B. A)",
        example = "A",
        maxLength = 10
    )
    private String row;
    
    @NotBlank(message = "Number is required")
    @Schema(
        description = "Sitznummer in Reihe (z.B. 12)",
        example = "12",
        maxLength = 10
    )
    private String number;
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Schema(
        description = "Preis des Sitzplatzes in EUR",
        example = "89.99"
    )
    private Double price;
    
    // ==================== CONSTRUCTORS ====================
    
    public CreateSeatRequestDTO() {}
    
    public CreateSeatRequestDTO(String seatNumber, String category, String block, String row, String number, Double price) {
        this.seatNumber = seatNumber;
        this.category = category;
        this.block = block;
        this.row = row;
        this.number = number;
        this.price = price;
    }
    
    // ==================== GETTERS & SETTERS ====================
    
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
    
    public String getRow() {
        return row;
    }
    
    public void setRow(String row) {
        this.row = row;
    }
    
    public String getNumber() {
        return number;
    }
    
    public void setNumber(String number) {
        this.number = number;
    }
    
    public Double getPrice() {
        return price;
    }
    
    public void setPrice(Double price) {
        this.price = price;
    }
}
