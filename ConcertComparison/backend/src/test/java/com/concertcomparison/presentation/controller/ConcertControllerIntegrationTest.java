package com.concertcomparison.presentation.controller;

import com.concertcomparison.domain.model.Concert;
import com.concertcomparison.domain.model.Seat;
import com.concertcomparison.domain.repository.ConcertRepository;
import com.concertcomparison.domain.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests für ConcertController (US-07).
 *
 * Testet den kompletten Stack von REST Controller bis Datenbank.
 * Verwendet H2 In-Memory DB für isolierte Tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("ConcertController Integration Tests")
class ConcertControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private SeatRepository seatRepository;

    @BeforeEach
    void setUp() {
        // Clean DB
        seatRepository.deleteAll();
        concertRepository.deleteAll();

        // Test Data: 2 Concerts
        Concert concert1 = Concert.createConcert(
            "The Weeknd Live",
            LocalDateTime.of(2026, 11, 1, 20, 0),
            "Berlin Arena",
            "After Hours Tour"
        );
        Concert concert2 = Concert.createConcert(
            "Coldplay Tour",
            LocalDateTime.of(2026, 11, 3, 19, 0),
            "Hamburg Stadium",
            "Music of the Spheres"
        );
        concert1 = concertRepository.save(concert1);
        concert2 = concertRepository.save(concert2);

        // Concert 1: Hat verfügbare Seats (99.99€)
        Seat seat1 = new Seat(
            concert1.getId(),
            "A-1-1",
            "VIP",
            "A",
            "1",
            "1",
            Double.valueOf(99.99)
        );
        seatRepository.save(seat1);

        // Concert 2: Alle Seats sold (79.99€)
        Seat seat2 = new Seat(
            concert2.getId(),
            "A-1-1",
            "Standard",
            "A",
            "1",
            "1",
            Double.valueOf(79.99)
        );
        seat2.hold("user1", 15);
        seat2.sell("user1");
        seatRepository.save(seat2);
    }

    @Test
    @DisplayName("GET /events - Sollte alle Concerts mit Pagination zurückgeben")
    void shouldReturnAllConcertsWithPagination() throws Exception {
        mockMvc.perform(get("/events")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page", is(0)))
            .andExpect(jsonPath("$.size", is(10)))
            .andExpect(jsonPath("$.totalElements", is(2)))
            .andExpect(jsonPath("$.items", hasSize(2)))
            .andExpect(jsonPath("$.items[0].name").value("The Weeknd Live"))
            .andExpect(jsonPath("$.items[0].available", is(true)))
            .andExpect(jsonPath("$.items[0].minPrice", is(99.99)))
            .andExpect(jsonPath("$.items[1].name").value("Coldplay Tour"))
            .andExpect(jsonPath("$.items[1].available", is(false)))
            .andExpect(jsonPath("$.items[1].minPrice", is(79.99)));
    }

    @Test
    @DisplayName("GET /events?date=... - Sollte nach Datum filtern")
    void shouldFilterByDate() throws Exception {
        mockMvc.perform(get("/events")
                .param("date", "2026-11-01"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", hasSize(1)))
            .andExpect(jsonPath("$.items[0].name").value("The Weeknd Live"))
            .andExpect(jsonPath("$.items[0].date").value("2026-11-01"));
    }

    @Test
    @DisplayName("GET /events?venue=... - Sollte nach Venue filtern (case-insensitive)")
    void shouldFilterByVenue() throws Exception {
        mockMvc.perform(get("/events")
                .param("venue", "berlin"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", hasSize(1)))
            .andExpect(jsonPath("$.items[0].venue").value("Berlin Arena"));
    }

    @Test
    @DisplayName("GET /events?minPrice=...&maxPrice=... - Sollte nach Preis-Range filtern")
    void shouldFilterByPriceRange() throws Exception {
        mockMvc.perform(get("/events")
                .param("minPrice", "70")
                .param("maxPrice", "90"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", hasSize(1)))
            .andExpect(jsonPath("$.items[0].name").value("Coldplay Tour"))
            .andExpect(jsonPath("$.items[0].minPrice", is(79.99)));
    }

    @Test
    @DisplayName("GET /events?sortBy=name&sortOrder=desc - Sollte nach Name sortieren")
    void shouldSortByName() throws Exception {
        mockMvc.perform(get("/events")
                .param("sortBy", "name")
                .param("sortOrder", "asc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].name").value("Coldplay Tour"))
            .andExpect(jsonPath("$.items[1].name").value("The Weeknd Live"));
    }

    @Test
    @DisplayName("GET /events?sortBy=date&sortOrder=desc - Sollte nach Datum absteigend sortieren")
    void shouldSortByDateDescending() throws Exception {
        mockMvc.perform(get("/events")
                .param("sortBy", "date")
                .param("sortOrder", "desc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].name").value("Coldplay Tour"))  // 2026-11-03
            .andExpect(jsonPath("$.items[1].name").value("The Weeknd Live")); // 2026-11-01
    }

    @Test
    @DisplayName("GET /events - Sollte Default-Parameter verwenden")
    void shouldUseDefaultParameters() throws Exception {
        mockMvc.perform(get("/events"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page", is(0)))
            .andExpect(jsonPath("$.size", is(20)))
            .andExpect(jsonPath("$.items", hasSize(2)));
    }

    @Test
    @DisplayName("GET /events?page=1&size=1 - Sollte Pagination korrekt anwenden")
    void shouldApplyPagination() throws Exception {
        mockMvc.perform(get("/events")
                .param("page", "1")
                .param("size", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page", is(1)))
            .andExpect(jsonPath("$.size", is(1)))
            .andExpect(jsonPath("$.items", hasSize(1)))
            .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    @DisplayName("GET /events - Sollte Verfügbarkeit korrekt anzeigen")
    void shouldShowCorrectAvailability() throws Exception {
        mockMvc.perform(get("/events"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].available", is(true)))  // The Weeknd: AVAILABLE Seat
            .andExpect(jsonPath("$.items[1].available", is(false))); // Coldplay: Alle Seats SOLD
    }

    @Test
    @DisplayName("GET /events - Sollte minPrice korrekt aggregieren")
    void shouldAggregateMinPrice() throws Exception {
        // Füge weiteren Seat mit höherem Preis hinzu
        Concert concert = concertRepository.findAll().get(0);
        Seat expensiveSeat = new Seat(
            concert.getId(),
            "B-1-1",
            "VIP Plus",
            "B",
            "1",
            "1",
            Double.valueOf(199.99)
        );
        seatRepository.save(expensiveSeat);

        mockMvc.perform(get("/events"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].minPrice", is(99.99))); // MIN sollte 99.99 sein, nicht 199.99
    }

    @Test
    @DisplayName("GET /events - Sollte valide Validation Constraints anwenden")
    void shouldValidateQueryParameters() throws Exception {
        // Negative page sollte abgelehnt werden
        mockMvc.perform(get("/events")
                .param("page", "-1"))
            .andExpect(status().isBadRequest());

        // Size > 200 sollte abgelehnt werden
        mockMvc.perform(get("/events")
                .param("size", "201"))
            .andExpect(status().isBadRequest());

        // Negative minPrice sollte abgelehnt werden
        mockMvc.perform(get("/events")
                .param("minPrice", "-10"))
            .andExpect(status().isBadRequest());
    }
}
