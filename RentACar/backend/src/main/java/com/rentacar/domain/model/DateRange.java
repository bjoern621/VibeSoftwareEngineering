package com.rentacar.domain.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Value Object für einen Zeitraum mit Start- und Enddatum.
 * 
 * Wird verwendet für Buchungszeiträume und Mietperioden.
 * Stellt sicher, dass der Zeitraum valide ist (Start vor Ende, nicht in der Vergangenheit).
 */
public class DateRange {
    
    private final LocalDateTime startDateTime;
    private final LocalDateTime endDateTime;
    
    /**
     * Erstellt einen neuen DateRange.
     * 
     * @param startDateTime Startdatum und -zeit
     * @param endDateTime Enddatum und -zeit
     * @throws IllegalArgumentException wenn die Validierung fehlschlägt
     */
    public DateRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        validateDateRange(startDateTime, endDateTime);
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }
    
    /**
     * Validiert den Zeitraum.
     * 
     * @param start Startdatum
     * @param end Enddatum
     * @throws IllegalArgumentException wenn die Validierung fehlschlägt
     */
    private void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null) {
            throw new com.rentacar.domain.exception.InvalidDateRangeException(
                "Startdatum darf nicht null sein");
        }
        if (end == null) {
            throw new com.rentacar.domain.exception.InvalidDateRangeException(
                "Enddatum darf nicht null sein");
        }
        if (!start.isBefore(end)) {
            throw new com.rentacar.domain.exception.InvalidDateRangeException(
                start, end, "Startdatum muss vor dem Enddatum liegen. Start: " + start + ", Ende: " + end);
        }
        if (start.isBefore(LocalDateTime.now())) {
            throw new com.rentacar.domain.exception.InvalidDateRangeException(
                start, end, "Startdatum darf nicht in der Vergangenheit liegen. Start: " + start);
        }
    }
    
    /**
     * Berechnet die Anzahl der vollständigen Tage im Zeitraum.
     * Wird aufgerundet (auch teilweise Tage zählen als voller Tag).
     * Mindestens 1 Tag wird zurückgegeben.
     * 
     * @return Anzahl der Miettage
     */
    public int getDays() {
        long hours = ChronoUnit.HOURS.between(startDateTime, endDateTime);
        // Aufrunden: Jede angefangene 24-Stunden-Periode zählt als voller Tag
        int days = (int) Math.ceil(hours / 24.0);
        return Math.max(1, days); // Mindestens 1 Tag
    }
    
    /**
     * @return Startdatum und -zeit
     */
    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }
    
    /**
     * @return Enddatum und -zeit
     */
    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }
    
    /**
     * Prüft, ob ein bestimmter Zeitpunkt innerhalb dieses Zeitraums liegt.
     * 
     * @param dateTime Der zu prüfende Zeitpunkt
     * @return true wenn der Zeitpunkt innerhalb des Zeitraums liegt
     */
    public boolean contains(LocalDateTime dateTime) {
        return !dateTime.isBefore(startDateTime) && !dateTime.isAfter(endDateTime);
    }
    
    /**
     * Prüft, ob sich zwei Zeiträume überschneiden.
     * 
     * @param other Der andere Zeitraum
     * @return true wenn sich die Zeiträume überschneiden
     */
    public boolean overlaps(DateRange other) {
        return !this.endDateTime.isBefore(other.startDateTime) 
            && !this.startDateTime.isAfter(other.endDateTime);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DateRange dateRange = (DateRange) o;
        return Objects.equals(startDateTime, dateRange.startDateTime) &&
               Objects.equals(endDateTime, dateRange.endDateTime);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(startDateTime, endDateTime);
    }
    
    @Override
    public String toString() {
        return "DateRange{" +
               "start=" + startDateTime +
               ", end=" + endDateTime +
               ", days=" + getDays() +
               '}';
    }
}
