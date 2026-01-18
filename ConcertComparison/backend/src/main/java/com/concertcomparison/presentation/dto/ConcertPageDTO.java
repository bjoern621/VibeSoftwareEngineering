package com.concertcomparison.presentation.dto;

import java.util.List;

/**
 * DTO für paginierte Concert-Liste (US-07).
 *
 * Presentation Layer: Response DTO gemäß OpenAPI Specification.
 * Folgt dem Standard Page-Response Pattern.
 */
public record ConcertPageDTO(
    int page,
    int size,
    long totalElements,
    List<ConcertListItemDTO> items
) {
}
