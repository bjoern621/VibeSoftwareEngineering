package com.travelreimburse.domain.model;

/**
 * Status-Enum für den Lebenszyklus eines Reiseantrags
 */
public enum TravelRequestStatus {
    /**
     * Entwurf - Antrag wurde angelegt, aber noch nicht eingereicht
     */
    DRAFT,
    
    /**
     * Eingereicht - Antrag wartet auf Genehmigung
     */
    SUBMITTED
}
