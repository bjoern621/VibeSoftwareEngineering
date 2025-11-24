package com.rentacar.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Entity für Schadensberichte.
 * Verknüpft mit einem Mietvertrag (und damit Buchung und Fahrzeug).
 */
@Entity
@Table(name = "damage_reports")
public class DamageReport {

    private static final int DESCRIPTION_LENGTH = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_agreement_id", nullable = false)
    private RentalAgreement rentalAgreement;

    @Column(nullable = false, length = DESCRIPTION_LENGTH)
    private String description;

    @Column(name = "estimated_cost")
    private BigDecimal estimatedCost;

    @ElementCollection
    @CollectionTable(name = "damage_report_photos", joinColumns = @JoinColumn(name = "damage_report_id"))
    @Column(name = "photo_url")
    private List<String> photos = new ArrayList<>();

    protected DamageReport() {
        // JPA
    }

    public DamageReport(RentalAgreement rentalAgreement, String description, BigDecimal estimatedCost, List<String> photos) {
        this.rentalAgreement = Objects.requireNonNull(rentalAgreement, "Rental agreement must not be null");
        this.description = Objects.requireNonNull(description, "Description must not be null");
        this.estimatedCost = estimatedCost;
        if (photos != null) {
            this.photos.addAll(photos);
        }
    }

    public Long getId() {
        return id;
    }

    public RentalAgreement getRentalAgreement() {
        return rentalAgreement;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getEstimatedCost() {
        return estimatedCost;
    }

    public List<String> getPhotos() {
        return Collections.unmodifiableList(photos);
    }
}
