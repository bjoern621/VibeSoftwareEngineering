package com.concertcomparison.presentation.controller;

import com.concertcomparison.domain.model.Concert;
import com.concertcomparison.domain.model.Seat;
import com.concertcomparison.domain.model.SeatStatus;
import com.concertcomparison.infrastructure.persistence.JpaConcertRepository;
import com.concertcomparison.infrastructure.persistence.JpaSeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("US-07 Concert List API Integration Tests")
class ConcertControllerUS07IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaConcertRepository concertRepository;

    @Autowired
    private JpaSeatRepository seatRepository;

    private Concert concertAlpha;
    private Concert concertBeta;
    private Concert concertGamma;

    @BeforeEach
    void setUp() {
        seatRepository.deleteAll();
        concertRepository.deleteAll();

        concertGamma = createConcert("Gamma", 1, "Hall One", "Cheap show");
        concertAlpha = createConcert("Alpha", 2, "Hall One", "Main show");
        concertBeta = createConcert("Beta", 3, "City Arena", "Premium show");

        // Gamma: 1 available seat price 30
        addSeat(concertGamma.getId(), "G-1", 30.0, SeatStatus.AVAILABLE);

        // Alpha: available seats with min 50, max 60
        addSeat(concertAlpha.getId(), "A-1", 50.0, SeatStatus.AVAILABLE);
        addSeat(concertAlpha.getId(), "A-2", 60.0, SeatStatus.AVAILABLE);

        // Beta: sold out, min 110, max 120
        addSeat(concertBeta.getId(), "B-1", 110.0, SeatStatus.SOLD);
        addSeat(concertBeta.getId(), "B-2", 120.0, SeatStatus.SOLD);
    }

    @Test
    @DisplayName("Filter by venue substring")
    void shouldFilterByVenue() throws Exception {
        mockMvc.perform(get("/api/concerts")
                .param("venue", "Hall")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.concerts", hasSize(2)))
            .andExpect(jsonPath("$.concerts[0].venue", is("Hall One")))
            .andExpect(jsonPath("$.page.totalElements", is(2)));
    }

    @Test
    @DisplayName("Sort by price descending uses min seat price")
    void shouldSortByMinPriceDescending() throws Exception {
        mockMvc.perform(get("/api/concerts")
                .param("sortBy", "price")
                .param("sortOrder", "desc")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.concerts[0].name", is("Beta")))
            .andExpect(jsonPath("$.concerts[0].minPrice", closeTo(110.0, 0.001)))
            .andExpect(jsonPath("$.concerts[0].availabilityStatus", is("SOLD_OUT")))
            .andExpect(jsonPath("$.concerts[1].name", is("Alpha")))
            .andExpect(jsonPath("$.concerts[1].minPrice", closeTo(50.0, 0.001)));
    }

    @Test
    @DisplayName("Pagination returns correct metadata and items")
    void shouldPaginateResults() throws Exception {
        mockMvc.perform(get("/api/concerts")
                .param("page", "0")
                .param("size", "1")
                .param("sortBy", "date")
                .param("sortOrder", "asc")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.concerts", hasSize(1)))
            .andExpect(jsonPath("$.concerts[0].name", is("Gamma")))
            .andExpect(jsonPath("$.page.page", is(0)))
            .andExpect(jsonPath("$.page.size", is(1)))
            .andExpect(jsonPath("$.page.totalElements", is(3)))
            .andExpect(jsonPath("$.page.totalPages", is(3)));
    }

    @SuppressWarnings("null")
    private Concert createConcert(String name, int daysFromNow, String venue, String description) {
        Concert concert = Concert.createConcert(
            name,
            LocalDateTime.now().plusDays(daysFromNow),
            venue,
            description
        );
        return concertRepository.saveAndFlush(concert);
    }

    @SuppressWarnings("null")
    private void addSeat(Long concertId, String seatNumber, double price, SeatStatus status) {
        Seat seat = new Seat(concertId, seatNumber, "CATEGORY", "Block A", "A", seatNumber, price);

        if (status == SeatStatus.HELD) {
            seat.hold("res-" + seatNumber, LocalDateTime.now().plusMinutes(5));
        } else if (status == SeatStatus.SOLD) {
            seat.hold("res-" + seatNumber, LocalDateTime.now().plusMinutes(5));
            seat.sell("res-" + seatNumber);
        }

        seatRepository.saveAndFlush(seat);
    }
}
