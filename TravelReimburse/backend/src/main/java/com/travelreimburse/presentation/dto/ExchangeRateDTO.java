package com.travelreimburse.presentation.dto;

import com.travelreimburse.domain.model.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO für Wechselkurs-Informationen
 */
public record ExchangeRateDTO(
    Currency fromCurrency,
    Currency toCurrency,
    BigDecimal rate,
    LocalDate date
) {
}

