package com.concertcomparison.domain.model;

/**
 * Status einer Bestellung (Order) im Concert Comparison System.
 */
public enum OrderStatus {
    
    /**
     * Bestellung wurde erstellt, Zahlung ausstehend.
     */
    PENDING("Ausstehend"),
    
    /**
     * Zahlung erfolgreich, Bestellung bestätigt.
     */
    CONFIRMED("Bestätigt"),
    
    /**
     * Bestellung wurde storniert.
     */
    CANCELLED("Storniert"),
    
    /**
     * Bestellung wurde zurückerstattet.
     */
    REFUNDED("Erstattet");
    
    private final String displayName;
    
    OrderStatus(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Liefert den deutschen Anzeigenamen für den Order-Status.
     * 
     * @return Anzeigename (z.B. "Ausstehend", "Bestätigt")
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Prüft ob die Bestellung erfolgreich abgeschlossen ist.
     * 
     * @return true wenn Status CONFIRMED
     */
    public boolean isConfirmed() {
        return this == CONFIRMED;
    }
    
    /**
     * Prüft ob die Bestellung storniert oder erstattet wurde.
     * 
     * @return true wenn Status CANCELLED oder REFUNDED
     */
    public boolean isCancelledOrRefunded() {
        return this == CANCELLED || this == REFUNDED;
    }
}
