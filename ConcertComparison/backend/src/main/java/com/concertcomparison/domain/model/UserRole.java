package com.concertcomparison.domain.model;

/**
 * Benutzerrollen im Concert Comparison System.
 */
public enum UserRole {
    
    /**
     * Normaler Benutzer (kann Tickets kaufen).
     */
    USER("Benutzer"),
    
    /**
     * Administrator (kann Konzerte und Seats verwalten).
     */
    ADMIN("Administrator");
    
    private final String displayName;
    
    UserRole(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Liefert den deutschen Anzeigenamen für die Benutzerrolle.
     * 
     * @return Anzeigename (z.B. "Benutzer", "Administrator")
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Prüft ob die Rolle Admin ist.
     * 
     * @return true wenn Role ADMIN
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }
}
