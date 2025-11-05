package com.travelreimburse.domain.model;

import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Value Object für einen Datumsbereich (unveränderlich)
 * Repräsentiert Start- und Enddatum einer Reise
 */
@Embeddable
public class DateRange {
    
    private LocalDate startDate;
    private LocalDate endDate;
    
    // JPA benötigt Default-Konstruktor
    protected DateRange() {
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
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
}
