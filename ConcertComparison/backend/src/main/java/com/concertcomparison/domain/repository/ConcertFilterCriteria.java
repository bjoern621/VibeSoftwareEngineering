package com.concertcomparison.domain.repository;

import java.time.LocalDate;

/**
 * Filterkriterien für Konzertabfragen mit dynamischen Parametern.
 * 
 * Wird vom Repository genutzt, um flexible WHERE-Klauseln aufzubauen.
 */
public record ConcertFilterCriteria(
    LocalDate date,
    String venue,
    Double minPrice,
    Double maxPrice
) {
    public boolean hasAnyPriceFilter() {
        return minPrice != null || maxPrice != null;
    }
}
