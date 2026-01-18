package com.concertcomparison.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

/**
 * DTO für Concert in der Liste (US-07).
 *
 * Presentation Layer: Response DTO gemäß OpenAPI Specification.
 * Enthält Verfügbarkeits-Indikator für Frontend.
 */
public record ConcertListItemDTO(
    String id,
    String name,
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate date,
    String venue,
    Double minPrice,
    boolean available
) {
}
