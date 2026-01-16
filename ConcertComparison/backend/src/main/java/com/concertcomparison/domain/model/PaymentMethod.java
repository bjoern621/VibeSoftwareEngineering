package com.concertcomparison.domain.model;

/**
 * Zahlungsmethoden für Ticket-Käufe.
 */
public enum PaymentMethod {
    
    /**
     * Kreditkarte (Visa, MasterCard, etc.)
     */
    CREDIT_CARD("Kreditkarte"),
    
    /**
     * PayPal
     */
    PAYPAL("PayPal"),
    
    /**
     * Sofortüberweisung
     */
    SOFORT("Sofortüberweisung"),
    
    /**
     * Banküberweisung
     */
    BANK_TRANSFER("Banküberweisung"),
    
    /**
     * Apple Pay
     */
    APPLE_PAY("Apple Pay"),
    
    /**
     * Google Pay
     */
    GOOGLE_PAY("Google Pay");
    
    private final String displayName;
    
    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Liefert den deutschen Anzeigenamen für die Zahlungsmethode.
     * 
     * @return Anzeigename (z.B. "Kreditkarte", "PayPal")
     */
    public String getDisplayName() {
        return displayName;
    }
}
