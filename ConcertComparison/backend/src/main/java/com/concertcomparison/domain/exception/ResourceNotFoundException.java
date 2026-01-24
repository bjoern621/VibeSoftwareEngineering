package com.concertcomparison.domain.exception;

/**
 * Base Domain Exception für Ressourcen-nicht-gefunden Fehler.
 * 
 * Wird geworfen, wenn eine angeforderte Ressource (Concert, Seat, Reservation, Order, User) nicht existiert.
 */
public class ResourceNotFoundException extends RuntimeException {
    
    private String resourceType;
    private String resourceId;

    /**
     * Erstellt eine neue ResourceNotFoundException mit Fehler- und Ressourcentyp.
     * 
     * @param message Benutzerfreundliche Fehlermeldung
     * @param resourceType Typ der nicht gefundenen Ressource (z.B. "Concert", "Seat")
     * @param resourceId ID der nicht gefundenen Ressource
     */
    public ResourceNotFoundException(String message, String resourceType, String resourceId) {
        super(message);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    /**
     * Erstellt eine neue ResourceNotFoundException mit Fehlermeldung.
     * 
     * @param message Benutzerfreundliche Fehlermeldung
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }
}
