package com.travelreimburse.infrastructure.external.exrat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Response DTO für ExRat API
 * Repräsentiert die Antwort des externen Währungskurs-Services
 */
public record ExRatResponse(
    String base,
    LocalDate date,
    Map<String, BigDecimal> rates
) {
}

