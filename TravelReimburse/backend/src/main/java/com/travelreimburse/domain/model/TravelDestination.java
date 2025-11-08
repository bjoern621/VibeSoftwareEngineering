package com.travelreimburse.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity für Reiseziele mit Visa- und Impfanforderungen
 * 
 * DDD: Rich Domain Model mit Business-Methoden
 * Aggregate Root für Destination-Informationen
 */
@Entity
@Table(name = "travel_destinations", 
       uniqueConstraints = @UniqueConstraint(columnNames = "country_code"))
public class TravelDestination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverride(name = "code", column = @Column(name = "country_code", nullable = false, unique = true))
    private CountryCode countryCode;

    @Column(nullable = false, length = 100)
    private String countryName;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "required", column = @Column(name = "visa_required")),
        @AttributeOverride(name = "type", column = @Column(name = "visa_type")),
        @AttributeOverride(name = "processingDays", column = @Column(name = "visa_processing_days")),
        @AttributeOverride(name = "notes", column = @Column(name = "visa_notes"))
    })
    private VisaRequirement visaRequirement;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "required", column = @Column(name = "vaccination_required")),
        @AttributeOverride(name = "requiredVaccinations", column = @Column(name = "vaccination_types")),
        @AttributeOverride(name = "recommendedVaccinations", column = @Column(name = "vaccination_recommended")),
        @AttributeOverride(name = "notes", column = @Column(name = "vaccination_notes"))
    })
    private VaccinationRequirement vaccinationRequirement;

    @Column(name = "general_travel_advice", length = 2000)
    private String generalTravelAdvice;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    // JPA benötigt Default-Konstruktor
    protected TravelDestination() {
    }

    /**
     * Erstellt ein neues Reiseziel mit Visa- und Impfanforderungen
     * 
     * @param countryCode ISO-Ländercode
     * @param countryName Name des Landes
     * @param visaRequirement Visa-Anforderungen
     * @param vaccinationRequirement Impfanforderungen
     * @param generalTravelAdvice Allgemeine Reisehinweise
     */
    public TravelDestination(CountryCode countryCode, String countryName,
                            VisaRequirement visaRequirement,
                            VaccinationRequirement vaccinationRequirement,
                            String generalTravelAdvice) {
        validateParameters(countryCode, countryName, visaRequirement, vaccinationRequirement);
        
        this.countryCode = countryCode;
        this.countryName = countryName;
        this.visaRequirement = visaRequirement;
        this.vaccinationRequirement = vaccinationRequirement;
        this.generalTravelAdvice = generalTravelAdvice;
        this.createdAt = LocalDateTime.now();
    }

    private void validateParameters(CountryCode countryCode, String countryName,
                                   VisaRequirement visaRequirement,
                                   VaccinationRequirement vaccinationRequirement) {
        if (countryCode == null) {
            throw new IllegalArgumentException("Ländercode darf nicht null sein");
        }
        if (countryName == null || countryName.trim().isEmpty()) {
            throw new IllegalArgumentException("Ländername darf nicht leer sein");
        }
        if (visaRequirement == null) {
            throw new IllegalArgumentException("Visa-Anforderungen dürfen nicht null sein");
        }
        if (vaccinationRequirement == null) {
            throw new IllegalArgumentException("Impfanforderungen dürfen nicht null sein");
        }
    }

    /**
     * Business-Methode: Aktualisiert die Visa-Anforderungen
     * 
     * DDD: Geschäftslogik in der Entity, nicht im Service
     */
    public void updateVisaRequirement(VisaRequirement newVisaRequirement) {
        if (newVisaRequirement == null) {
            throw new IllegalArgumentException("Visa-Anforderungen dürfen nicht null sein");
        }
        this.visaRequirement = newVisaRequirement;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Business-Methode: Aktualisiert die Impfanforderungen
     * 
     * DDD: Geschäftslogik in der Entity, nicht im Service
     */
    public void updateVaccinationRequirement(VaccinationRequirement newVaccinationRequirement) {
        if (newVaccinationRequirement == null) {
            throw new IllegalArgumentException("Impfanforderungen dürfen nicht null sein");
        }
        this.vaccinationRequirement = newVaccinationRequirement;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Business-Methode: Aktualisiert allgemeine Reisehinweise
     */
    public void updateGeneralTravelAdvice(String advice) {
        this.generalTravelAdvice = advice;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Business-Methode: Prüft ob das Reiseziel eine spezielle Vorbereitung erfordert
     */
    public boolean requiresPreparation() {
        return visaRequirement.isRequired() || vaccinationRequirement.isRequired();
    }

    /**
     * Business-Methode: Prüft ob genug Zeit für Visa-Beantragung vorhanden ist
     * 
     * @param daysUntilTravel Tage bis zur Reise
     * @return true wenn genug Zeit oder kein Visum erforderlich
     */
    public boolean hasEnoughPreparationTime(long daysUntilTravel) {
        return visaRequirement.hasEnoughTimeForProcessing(daysUntilTravel);
    }

    /**
     * Business-Methode: Gibt eine Zusammenfassung der Anforderungen zurück
     */
    public String getRequirementsSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Land: ").append(countryName).append(" (").append(countryCode).append(")\n");
        
        if (countryCode.isEuropeanUnion()) {
            summary.append("EU-Land: Keine Visa-Anforderungen\n");
        } else {
            summary.append("Visum: ").append(visaRequirement).append("\n");
        }
        
        summary.append("Impfungen: ").append(vaccinationRequirement);
        
        return summary.toString();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public CountryCode getCountryCode() {
        return countryCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public VisaRequirement getVisaRequirement() {
        return visaRequirement;
    }

    public VaccinationRequirement getVaccinationRequirement() {
        return vaccinationRequirement;
    }

    public String getGeneralTravelAdvice() {
        return generalTravelAdvice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TravelDestination that = (TravelDestination) o;
        return Objects.equals(countryCode, that.countryCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(countryCode);
    }

    @Override
    public String toString() {
        return String.format("TravelDestination{%s - %s}", countryCode, countryName);
    }
}
