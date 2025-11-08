package com.travelreimburse.domain.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.Objects;

/**
 * Value Object für Abwesenheitsinformationen aus HRIS
 * Unveränderlich (immutable)
 */
@Embeddable
public class AbsenceInfo {

    private String absenceId;
    
    @Enumerated(EnumType.STRING)
    private AbsenceType type;
    
    @Embedded
    private DateRange period;
    
    @Enumerated(EnumType.STRING)
    private AbsenceStatus status;
    
    private String reason;

    // JPA benötigt Default-Konstruktor
    protected AbsenceInfo() {
    }

    public AbsenceInfo(String absenceId, AbsenceType type, DateRange period, 
                       AbsenceStatus status, String reason) {
        if (absenceId == null || absenceId.trim().isEmpty()) {
            throw new IllegalArgumentException("AbsenceId darf nicht leer sein");
        }
        if (type == null) {
            throw new IllegalArgumentException("AbsenceType darf nicht null sein");
        }
        if (period == null) {
            throw new IllegalArgumentException("Period darf nicht null sein");
        }
        if (status == null) {
            throw new IllegalArgumentException("AbsenceStatus darf nicht null sein");
        }
        
        this.absenceId = absenceId;
        this.type = type;
        this.period = period;
        this.status = status;
        this.reason = reason;
    }

    /**
     * Prüft ob diese Abwesenheit mit einem Reisezeitraum überlappt
     */
    public boolean overlaps(DateRange travelPeriod) {
        return this.period.overlaps(travelPeriod);
    }

    /**
     * Prüft ob diese Abwesenheit für Validierung relevant ist
     * Nur APPROVED Abwesenheiten zählen
     * BUSINESS_TRIP wird ignoriert (bereits Dienstreise)
     */
    public boolean isRelevantForValidation() {
        return status == AbsenceStatus.APPROVED && 
               type != AbsenceType.BUSINESS_TRIP;
    }

    // Getter (keine Setter - immutable!)
    
    public String getAbsenceId() {
        return absenceId;
    }

    public AbsenceType getType() {
        return type;
    }

    public DateRange getPeriod() {
        return period;
    }

    public AbsenceStatus getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbsenceInfo that = (AbsenceInfo) o;
        return Objects.equals(absenceId, that.absenceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(absenceId);
    }

    @Override
    public String toString() {
        return "AbsenceInfo{" +
                "absenceId='" + absenceId + '\'' +
                ", type=" + type +
                ", period=" + period +
                ", status=" + status +
                ", reason='" + reason + '\'' +
                '}';
    }
}
