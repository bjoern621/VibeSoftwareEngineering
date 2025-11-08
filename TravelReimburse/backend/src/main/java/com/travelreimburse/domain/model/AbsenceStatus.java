package com.travelreimburse.domain.model;

/**
 * Status einer Abwesenheit im HRIS
 */
public enum AbsenceStatus {
    PENDING,        // Beantragt, noch nicht genehmigt
    APPROVED,       // Genehmigt
    REJECTED,       // Abgelehnt
    CANCELLED       // Storniert
}
