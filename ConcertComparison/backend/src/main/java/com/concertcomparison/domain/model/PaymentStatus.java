package com.concertcomparison.domain.model;

/**
 * Status einer Zahlung im Concert Comparison System.
 */
public enum PaymentStatus {
    
    /**
     * Zahlung wurde initiiert, wartet auf Bestätigung.
     */
    PENDING("Ausstehend"),
    
    /**
     * Zahlung wurde erfolgreich abgeschlossen.
     */
    COMPLETED("Abgeschlossen"),
    
    /**
     * Zahlung ist fehlgeschlagen.
     */
    FAILED("Fehlgeschlagen"),
    
    /**
     * Zahlung wurde storniert.
     */
    CANCELLED("Storniert"),
    
    /**
     * Zahlung wurde zurückerstattet.
     */
    REFUNDED("Erstattet");
    
    private final String displayName;
    
    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Liefert den deutschen Anzeigenamen für den Payment-Status.
     * 
     * @return Anzeigename (z.B. "Ausstehend", "Abgeschlossen")
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Prüft ob die Zahlung erfolgreich war.
     * 
     * @return true wenn Status COMPLETED
     */
    public boolean isSuccessful() {
        return this == COMPLETED;
    }
    
    /**
     * Prüft ob die Zahlung fehlgeschlagen ist.
     * 
     * @return true wenn Status FAILED oder CANCELLED
     */
    public boolean isFailed() {
        return this == FAILED || this == CANCELLED;
    }
}
