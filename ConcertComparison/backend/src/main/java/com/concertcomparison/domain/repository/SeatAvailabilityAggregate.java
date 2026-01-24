package com.concertcomparison.domain.repository;

/**
 * Aggregiertes Availability-Resultat für ein Konzert.
 */
public record SeatAvailabilityAggregate(
    Long concertId,
    long availableSeats,
    long totalSeats,
    Double minPrice,
    Double maxPrice
) {
    public boolean isSoldOut() {
        return totalSeats > 0 && availableSeats == 0;
    }
}
