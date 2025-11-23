package com.rentacar.presentation.dto;

import java.math.BigDecimal;
import java.util.List;

public class DamageReportResponseDTO {

    private Long id;
    private Long rentalAgreementId;
    private Long bookingId;
    private Long vehicleId;
    private String description;
    private BigDecimal estimatedCost;
    private List<String> photos;

    public DamageReportResponseDTO(Long id, Long rentalAgreementId, Long bookingId, Long vehicleId, String description, BigDecimal estimatedCost, List<String> photos) {
        this.id = id;
        this.rentalAgreementId = rentalAgreementId;
        this.bookingId = bookingId;
        this.vehicleId = vehicleId;
        this.description = description;
        this.estimatedCost = estimatedCost;
        this.photos = photos;
    }

    public Long getId() {
        return id;
    }

    public Long getRentalAgreementId() {
        return rentalAgreementId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getEstimatedCost() {
        return estimatedCost;
    }

    public List<String> getPhotos() {
        return photos;
    }
}
