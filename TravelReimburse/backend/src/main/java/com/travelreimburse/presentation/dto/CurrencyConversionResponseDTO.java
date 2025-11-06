package com.travelreimburse.presentation.dto;

import com.travelreimburse.domain.model.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO für Währungsumrechnung
 */
public record CurrencyConversionResponseDTO(
    BigDecimal originalAmount,
    Currency originalCurrency,
    BigDecimal convertedAmount,
    Currency convertedCurrency,
    BigDecimal exchangeRate,
    LocalDate date
) {
}

