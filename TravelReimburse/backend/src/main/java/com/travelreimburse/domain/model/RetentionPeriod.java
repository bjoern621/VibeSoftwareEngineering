package com.travelreimburse.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;

/**
 * Value Object für gesetzliche Aufbewahrungsfristen.
 * Immutable - repräsentiert die Frist nach §147 AO (10 Jahre).
 */
@Embeddable
public class RetentionPeriod {

    private static final int DEFAULT_RETENTION_YEARS = 10;

    @Column(name = "archived_at")
    private LocalDate archivedAt;

    @Column(name = "retention_end_date")
    private LocalDate retentionEndDate;

    @Column(name = "retention_years", nullable = true)
    private Integer retentionYears;

    protected RetentionPeriod() {
        // JPA - Default-Konstruktor
        this.retentionYears = null;  // Erlaubt NULL für neue Entities
    }

    private RetentionPeriod(LocalDate archivedAt, int retentionYears) {
        if (archivedAt == null) {
            throw new IllegalArgumentException("Archivierungsdatum darf nicht null sein");
        }
        if (retentionYears < 1) {
            throw new IllegalArgumentException("Aufbewahrungsfrist muss mindestens 1 Jahr betragen");
        }

        this.archivedAt = archivedAt;
        this.retentionYears = Integer.valueOf(retentionYears);
        this.retentionEndDate = archivedAt.plusYears(retentionYears);
    }

    /**
     * Erstellt Standard-Aufbewahrungsfrist (10 Jahre gemäß §147 AO)
     */
    public static RetentionPeriod standard() {
        return new RetentionPeriod(LocalDate.now(), DEFAULT_RETENTION_YEARS);
    }

    /**
     * Erstellt benutzerdefinierte Aufbewahrungsfrist
     */
    public static RetentionPeriod custom(LocalDate archivedAt, int retentionYears) {
        return new RetentionPeriod(archivedAt, retentionYears);
    }

    /**
     * Prüft, ob die Aufbewahrungsfrist abgelaufen ist
     */
    public boolean isExpired() {
        return LocalDate.now().isAfter(retentionEndDate);
    }

    /**
     * Berechnet verbleibende Tage bis Fristende
     */
    public long getRemainingDays() {
        return LocalDate.now().until(retentionEndDate, java.time.temporal.ChronoUnit.DAYS);
    }

    public LocalDate getArchivedAt() {
        return archivedAt;
    }

    public LocalDate getRetentionEndDate() {
        return retentionEndDate;
    }

    public Integer getRetentionYears() {
        return retentionYears;
    }
}

