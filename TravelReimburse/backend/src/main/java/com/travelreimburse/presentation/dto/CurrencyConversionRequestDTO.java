package com.travelreimburse.presentation.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.travelreimburse.domain.model.Currency;
import com.travelreimburse.infrastructure.serialization.FlexibleBigDecimalDeserializer;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO für Währungsumrechnung
 * Akzeptiert beide Dezimalformate: "20.00" und "20,00"
 */
public record CurrencyConversionRequestDTO(
    @NotNull(message = "Betrag darf nicht null sein")
    @DecimalMin(value = "0.01", message = "Betrag muss positiv sein")
    @JsonDeserialize(using = FlexibleBigDecimalDeserializer.class)
    BigDecimal amount,

    @NotNull(message = "Quellwährung darf nicht null sein")
    Currency fromCurrency,

    @NotNull(message = "Zielwährung darf nicht null sein")
    Currency toCurrency,

    LocalDate date  // Optional - wenn null, wird heutiges Datum verwendet
) {
}

