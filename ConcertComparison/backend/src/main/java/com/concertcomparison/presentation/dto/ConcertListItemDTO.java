package com.concertcomparison.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Listen-DTO für ein Konzert inkl. Verfügbarkeitsindikator")
public class ConcertListItemDTO {

    private String id;
    private String name;
    private LocalDateTime date;
    private String venue;
    private String description;
    private long totalSeats;
    private long availableSeats;
    private Double minPrice;
    private Double maxPrice;
    private String availabilityStatus;

    public ConcertListItemDTO() {}

    private ConcertListItemDTO(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.date = builder.date;
        this.venue = builder.venue;
        this.description = builder.description;
        this.totalSeats = builder.totalSeats;
        this.availableSeats = builder.availableSeats;
        this.minPrice = builder.minPrice;
        this.maxPrice = builder.maxPrice;
        this.availabilityStatus = builder.availabilityStatus;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String name;
        private LocalDateTime date;
        private String venue;
        private String description;
        private long totalSeats;
        private long availableSeats;
        private Double minPrice;
        private Double maxPrice;
        private String availabilityStatus;

        public Builder id(String id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder date(LocalDateTime date) { this.date = date; return this; }
        public Builder venue(String venue) { this.venue = venue; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder totalSeats(long totalSeats) { this.totalSeats = totalSeats; return this; }
        public Builder availableSeats(long availableSeats) { this.availableSeats = availableSeats; return this; }
        public Builder minPrice(Double minPrice) { this.minPrice = minPrice; return this; }
        public Builder maxPrice(Double maxPrice) { this.maxPrice = maxPrice; return this; }
        public Builder availabilityStatus(String availabilityStatus) { this.availabilityStatus = availabilityStatus; return this; }

        public ConcertListItemDTO build() { return new ConcertListItemDTO(this); }
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public LocalDateTime getDate() { return date; }
    public String getVenue() { return venue; }
    public String getDescription() { return description; }
    public long getTotalSeats() { return totalSeats; }
    public long getAvailableSeats() { return availableSeats; }
    public Double getMinPrice() { return minPrice; }
    public Double getMaxPrice() { return maxPrice; }
    public String getAvailabilityStatus() { return availabilityStatus; }
}
