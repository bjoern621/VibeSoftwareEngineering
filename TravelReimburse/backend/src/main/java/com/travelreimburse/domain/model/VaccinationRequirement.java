package com.travelreimburse.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;

/**
 * Value Object für Impfanforderungen
 * Unveränderlich - repräsentiert die Impfpflicht für ein Reiseziel
 * 
 * DDD: Immutable Value Object ohne Identität
 */
@Embeddable
public class VaccinationRequirement {

    @Column(name = "vaccination_required", nullable = false)
    private final boolean required;

    @Column(name = "vaccination_types", length = 500)
    private final String requiredVaccinations;

    @Column(name = "vaccination_recommended", length = 500)
    private final String recommendedVaccinations;

    @Column(name = "vaccination_notes", length = 1000)
    private final String notes;

    // JPA benötigt Default-Konstruktor
    protected VaccinationRequirement() {
        this.required = false;
        this.requiredVaccinations = null;
        this.recommendedVaccinations = null;
        this.notes = null;
    }

    /**
     * Erstellt eine Impfanforderung wenn keine Impfungen erforderlich sind
     */
    public static VaccinationRequirement notRequired() {
        return new VaccinationRequirement(false, null, null, null);
    }

    /**
     * Erstellt eine Impfanforderung wenn Impfungen erforderlich sind
     * 
     * @param requiredVaccinations Pflichtimpfungen (kommasepariert)
     * @param recommendedVaccinations Empfohlene Impfungen (kommasepariert)
     * @param notes Zusätzliche Hinweise
     */
    public static VaccinationRequirement required(String requiredVaccinations, 
                                                  String recommendedVaccinations, 
                                                  String notes) {
        if (requiredVaccinations == null || requiredVaccinations.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Pflichtimpfungen müssen angegeben werden wenn Impfungen erforderlich sind"
            );
        }
        return new VaccinationRequirement(true, requiredVaccinations.trim(), 
                                         recommendedVaccinations != null ? recommendedVaccinations.trim() : null, 
                                         notes);
    }

    /**
     * Erstellt eine Impfempfehlung (nicht verpflichtend)
     */
    public static VaccinationRequirement recommended(String recommendedVaccinations, String notes) {
        if (recommendedVaccinations == null || recommendedVaccinations.trim().isEmpty()) {
            throw new IllegalArgumentException("Empfohlene Impfungen müssen angegeben werden");
        }
        return new VaccinationRequirement(false, null, recommendedVaccinations.trim(), notes);
    }

    private VaccinationRequirement(boolean required, String requiredVaccinations, 
                                  String recommendedVaccinations, String notes) {
        this.required = required;
        this.requiredVaccinations = requiredVaccinations;
        this.recommendedVaccinations = recommendedVaccinations;
        this.notes = notes;
    }

    /**
     * Gibt alle Impfungen (Pflicht + Empfohlen) als Liste zurück
     */
    public String getAllVaccinations() {
        StringBuilder result = new StringBuilder();
        if (requiredVaccinations != null && !requiredVaccinations.isEmpty()) {
            result.append("Pflicht: ").append(requiredVaccinations);
        }
        if (recommendedVaccinations != null && !recommendedVaccinations.isEmpty()) {
            if (result.length() > 0) {
                result.append("; ");
            }
            result.append("Empfohlen: ").append(recommendedVaccinations);
        }
        return result.toString();
    }

    public boolean isRequired() {
        return required;
    }

    public String getRequiredVaccinations() {
        return requiredVaccinations;
    }

    public String getRecommendedVaccinations() {
        return recommendedVaccinations;
    }

    public String getNotes() {
        return notes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VaccinationRequirement that = (VaccinationRequirement) o;
        return required == that.required && 
               Objects.equals(requiredVaccinations, that.requiredVaccinations) && 
               Objects.equals(recommendedVaccinations, that.recommendedVaccinations) && 
               Objects.equals(notes, that.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(required, requiredVaccinations, recommendedVaccinations, notes);
    }

    @Override
    public String toString() {
        if (!required && (recommendedVaccinations == null || recommendedVaccinations.isEmpty())) {
            return "Keine Impfungen erforderlich";
        }
        return getAllVaccinations();
    }
}
