package com.travelreimburse.domain.model;

import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Value Object für einen Datumsbereich (unveränderlich)
 * Repräsentiert Start- und Enddatum einer Reise
 * 
 * ✅ DDD: Value Object - immutable (final fields)
 */
@Embeddable
public class DateRange {
    
    private final LocalDate startDate;
    private final LocalDate endDate;
    
    // JPA benötigt Default-Konstruktor (nur für Framework-Zugriff)
    // ✅ DDD: Final fields werden hier mit null initialisiert (JPA setzt echte Werte via Reflection)
    protected DateRange() {
        this.startDate = null;
        this.endDate = null;
    }
    
    public DateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start- und Enddatum dürfen nicht null sein");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Enddatum muss nach oder gleich Startdatum liegen");
        }
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    /**
     * Berechnet die Anzahl der Tage (inklusive Start- und Enddatum)
     */
    public long getDays() {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    /**
     * Prüft ob dieser Zeitraum mit einem anderen überlappt
     */
    public boolean overlaps(DateRange other) {
        if (other == null) {
            return false;
        }
        return !this.endDate.isBefore(other.startDate) && 
               !other.endDate.isBefore(this.startDate);
    }

    /**
     * Prüft ob ein bestimmtes Datum in diesem Zeitraum liegt
     */
    public boolean contains(LocalDate date) {
        if (date == null) {
            return false;
        }
        return !date.isBefore(this.startDate) && !date.isAfter(this.endDate);
    }

    /**
     * Prüft ob der Zeitraum in der Zukunft liegt
     */
    public boolean isInFuture() {
        return this.startDate.isAfter(LocalDate.now());
    }

    /**
     * Prüft ob der Zeitraum in der Vergangenheit liegt
     */
    public boolean isInPast() {
        return this.endDate.isBefore(LocalDate.now());
    }

    /**
     * Prüft ob der Zeitraum aktuell läuft (heute ist im Zeitraum)
     */
    public boolean isCurrent() {
        return contains(LocalDate.now());
    }

    /**
     * Berechnet Tage bis zum Start des Zeitraums
     */
    public long getDaysUntilStart() {
        return ChronoUnit.DAYS.between(LocalDate.now(), this.startDate);
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
}
