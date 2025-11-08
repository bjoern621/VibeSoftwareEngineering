package com.travelreimburse.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.Objects;

/**
 * Value Object für Visa-Anforderungen
 * Unveränderlich - repräsentiert die Visum-Pflicht für ein Reiseziel
 * 
 * DDD: Immutable Value Object ohne Identität
 */
@Embeddable
public class VisaRequirement {

    @Column(name = "visa_required", nullable = false)
    private final boolean required;

    @Enumerated(EnumType.STRING)
    @Column(name = "visa_type", length = 50)
    private final VisaType type;

    @Column(name = "visa_processing_days")
    private final Integer processingDays;

    @Column(name = "visa_notes", length = 1000)
    private final String notes;

    // JPA benötigt Default-Konstruktor
    protected VisaRequirement() {
        this.required = false;
        this.type = null;
        this.processingDays = null;
        this.notes = null;
    }

    /**
     * Erstellt eine Visa-Anforderung wenn kein Visum erforderlich ist
     */
    public static VisaRequirement notRequired() {
        return new VisaRequirement(false, null, null, null);
    }

    /**
     * Erstellt eine Visa-Anforderung wenn ein Visum erforderlich ist
     * 
     * @param type Art des Visums
     * @param processingDays Bearbeitungszeit in Tagen
     * @param notes Zusätzliche Hinweise
     */
    public static VisaRequirement required(VisaType type, Integer processingDays, String notes) {
        if (type == null) {
            throw new IllegalArgumentException("Visum-Typ muss angegeben werden wenn Visum erforderlich ist");
        }
        if (processingDays != null && processingDays < 0) {
            throw new IllegalArgumentException("Bearbeitungszeit darf nicht negativ sein");
        }
        return new VisaRequirement(true, type, processingDays, notes);
    }

    private VisaRequirement(boolean required, VisaType type, Integer processingDays, String notes) {
        this.required = required;
        this.type = type;
        this.processingDays = processingDays;
        this.notes = notes;
    }

    /**
     * Prüft ob genug Zeit für die Visa-Beantragung vorhanden ist
     * 
     * @param daysUntilTravel Tage bis zur Reise
     * @return true wenn genug Zeit, false wenn zu wenig Zeit
     */
    public boolean hasEnoughTimeForProcessing(long daysUntilTravel) {
        if (!required || processingDays == null) {
            return true;
        }
        return daysUntilTravel >= processingDays;
    }

    public boolean isRequired() {
        return required;
    }

    public VisaType getType() {
        return type;
    }

    public Integer getProcessingDays() {
        return processingDays;
    }

    public String getNotes() {
        return notes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VisaRequirement that = (VisaRequirement) o;
        return required == that.required && 
               Objects.equals(type, that.type) && 
               Objects.equals(processingDays, that.processingDays) && 
               Objects.equals(notes, that.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(required, type, processingDays, notes);
    }

    @Override
    public String toString() {
        if (!required) {
            return "Kein Visum erforderlich";
        }
        return String.format("Visum erforderlich: %s, Bearbeitungszeit: %d Tage", 
                           type, processingDays);
    }
}
