package com.travelreimburse.application.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.travelreimburse.infrastructure.serialization.FlexibleBigDecimalDeserializer;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO für das Aktualisieren eines Receipts.
 * Nur beschreibende Felder können aktualisiert werden.
 * Akzeptiert beide Dezimalformate: "20.00" und "20,00"
 */
public record UpdateReceiptDTO(
        @Size(max = 500, message = "Beschreibung darf maximal 500 Zeichen lang sein")
        String description,

        @DecimalMin(value = "0.01", message = "Betrag muss positiv sein")
        @JsonDeserialize(using = FlexibleBigDecimalDeserializer.class)
        BigDecimal amount,

        String currency,

        @Size(max = 100, message = "Anbieter darf maximal 100 Zeichen lang sein")
        String vendor
) {
}

