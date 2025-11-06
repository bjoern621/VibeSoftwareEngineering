package com.travelreimburse.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO für das Hinzufügen eines Reiseabschnitts zu einem Reiseantrag
 */
public record AddTravelLegDTO(
    @NotBlank(message = "Abfahrtsort darf nicht leer sein")
    String departureLocation,
    
    @NotBlank(message = "Ankunftsort darf nicht leer sein")
    String arrivalLocation,
    
    @NotNull(message = "Verkehrsmittel muss angegeben werden")
    String transportationType,
    
    @NotNull(message = "Kosten müssen angegeben werden")
    @DecimalMin(value = "0.0", inclusive = true, message = "Kosten dürfen nicht negativ sein")
    BigDecimal costAmount,
    
    @NotNull(message = "Währung muss angegeben werden")
    String currency,
    
    String description,
    
    LocalDateTime departureDateTime,
    
    LocalDateTime arrivalDateTime,
    
    Double distanceKm
) {
    /**
     * Validierung im Record-Konstruktor
     */
    public AddTravelLegDTO {
        if (distanceKm != null && distanceKm < 0) {
            throw new IllegalArgumentException("Distanz darf nicht negativ sein");
        }
        
        if (departureDateTime != null && arrivalDateTime != null && 
            departureDateTime.isAfter(arrivalDateTime)) {
            throw new IllegalArgumentException("Abfahrtszeit muss vor Ankunftszeit liegen");
        }
    }
}
