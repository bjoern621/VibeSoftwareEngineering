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
    SUBMITTED,

    /**
     * Genehmigt - Antrag wurde von Führungskraft genehmigt
     */
    APPROVED,

    /**
     * Abgelehnt - Antrag wurde von Fhrungskraft abgelehnt
     */
    REJECTED,

    /**
     * Ausgezahlt - Reisekosten wurden ausgezahlt
     */
    PAID,

    /**
     * Archiviert - Abgeschlossene Reise wurde archiviert
     */
    ARCHIVED
}
