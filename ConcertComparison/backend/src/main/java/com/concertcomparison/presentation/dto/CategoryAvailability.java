package com.concertcomparison.presentation.dto;

/**
 * DTO für aggregierte Verfügbarkeit einer Seat-Kategorie.
 * 
 * Liefert Zusammenfassung über alle Seats einer Kategorie:
 * - Gesamtzahl
 * - Verfügbar
 * - Reserviert
 * - Verkauft
 */
public class CategoryAvailability {
    
    private String category;
    private long total;
    private long available;
    private long held;
    private long sold;
    
    // ==================== CONSTRUCTORS ====================
    
    public CategoryAvailability() {
        // Default Constructor für Jackson Deserialization
    }
    
    public CategoryAvailability(String category, long total, long available, long held, long sold) {
        this.category = category;
        this.total = total;
        this.available = available;
        this.held = held;
        this.sold = sold;
        
        // Validierung: Keine negativen Werte erlaubt (Acceptance Criteria!)
        if (total < 0 || available < 0 || held < 0 || sold < 0) {
            throw new IllegalArgumentException("Negative Werte sind nicht erlaubt in CategoryAvailability");
        }
        
        // Validierung: Summe muss stimmen
        if (available + held + sold != total) {
            throw new IllegalArgumentException(
                String.format("Inkonsistente Summe in CategoryAvailability: %d + %d + %d != %d", 
                    available, held, sold, total)
            );
        }
    }
    
    // ==================== GETTERS & SETTERS ====================
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public long getTotal() {
        return total;
    }
    
    public void setTotal(long total) {
        this.total = total;
    }
    
    public long getAvailable() {
        return available;
    }
    
    public void setAvailable(long available) {
        this.available = available;
    }
    
    public long getHeld() {
        return held;
    }
    
    public void setHeld(long held) {
        this.held = held;
    }
    
    public long getSold() {
        return sold;
    }
    
    public void setSold(long sold) {
        this.sold = sold;
    }
}
