package com.concertcomparison.application.service;

import com.concertcomparison.domain.model.Concert;
import com.concertcomparison.domain.model.SeatStatus;
import com.concertcomparison.domain.repository.ConcertRepository;
import com.concertcomparison.domain.repository.SeatRepository;
import com.concertcomparison.presentation.dto.ConcertPageDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests für ConcertApplicationService (US-07).
 *
 * Testet Business Logic für Concert-Liste mit Filtern, Sortierung und Pagination.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConcertApplicationService Tests")
class ConcertApplicationServiceTest {

    @Mock
    private ConcertRepository concertRepository;

    @Mock
    private SeatRepository seatRepository;

    private ConcertApplicationService service;

    @BeforeEach
    void setUp() {
        service = new ConcertApplicationService(concertRepository, seatRepository);
    }

    @Test
    @DisplayName("Sollte paginierte Concerts mit Availability zurückgeben")
    void shouldReturnPagedConcertsWithAvailability() {
        // Given
        Concert concert1 = createConcert(1L, "The Weeknd", LocalDateTime.of(2026, 11, 1, 20, 0), "Berlin Arena");
        Concert concert2 = createConcert(2L, "Coldplay", LocalDateTime.of(2026, 11, 3, 19, 0), "Hamburg Stadium");

        Page<Concert> mockPage = new PageImpl<>(
            List.of(concert1, concert2),
            Pageable.unpaged(),
            2
        );

        when(concertRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(mockPage);

        // Concert 1: Available + Price 99.99
        when(seatRepository.existsByConcertIdAndStatus(1L, SeatStatus.AVAILABLE))
            .thenReturn(true);
        when(seatRepository.findMinPriceByConcertId(1L))
            .thenReturn(99.99);

        // Concert 2: Not Available + Price 79.99
        when(seatRepository.existsByConcertIdAndStatus(2L, SeatStatus.AVAILABLE))
            .thenReturn(false);
        when(seatRepository.findMinPriceByConcertId(2L))
            .thenReturn(79.99);

        // When
        ConcertPageDTO result = service.getAllConcerts(
            null, null, null, null, "date", "asc", 0, 20
        );

        // Then
        assertThat(result.items()).hasSize(2);
        assertThat(result.totalElements()).isEqualTo(2);
        assertThat(result.page()).isEqualTo(0);
        assertThat(result.size()).isEqualTo(20);

        // Concert 1
        assertThat(result.items().get(0).id()).isEqualTo("1");
        assertThat(result.items().get(0).name()).isEqualTo("The Weeknd");
        assertThat(result.items().get(0).available()).isTrue();
        assertThat(result.items().get(0).minPrice()).isEqualTo(99.99);

        // Concert 2
        assertThat(result.items().get(1).id()).isEqualTo("2");
        assertThat(result.items().get(1).name()).isEqualTo("Coldplay");
        assertThat(result.items().get(1).available()).isFalse();
        assertThat(result.items().get(1).minPrice()).isEqualTo(79.99);

        verify(concertRepository).findAll(any(Specification.class), any(Pageable.class));
        verify(seatRepository, times(2)).existsByConcertIdAndStatus(anyLong(), eq(SeatStatus.AVAILABLE));
        verify(seatRepository, times(2)).findMinPriceByConcertId(anyLong());
    }

    @Test
    @DisplayName("Sollte nach Datum filtern")
    void shouldFilterByDate() {
        // Given
        LocalDate filterDate = LocalDate.of(2026, 11, 1);
        Concert concert = createConcert(1L, "The Weeknd", LocalDateTime.of(2026, 11, 1, 20, 0), "Berlin Arena");

        Page<Concert> mockPage = new PageImpl<>(List.of(concert));

        when(concertRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(mockPage);
        when(seatRepository.existsByConcertIdAndStatus(anyLong(), any()))
            .thenReturn(true);
        when(seatRepository.findMinPriceByConcertId(anyLong()))
            .thenReturn(99.99);

        // When
        ConcertPageDTO result = service.getAllConcerts(
            filterDate, null, null, null, "date", "asc", 0, 20
        );

        // Then
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).date()).isEqualTo(filterDate);
    }

    @Test
    @DisplayName("Sollte nach Venue filtern")
    void shouldFilterByVenue() {
        // Given
        Concert concert = createConcert(1L, "The Weeknd", LocalDateTime.of(2026, 11, 1, 20, 0), "Berlin Arena");

        Page<Concert> mockPage = new PageImpl<>(List.of(concert));

        when(concertRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(mockPage);
        when(seatRepository.existsByConcertIdAndStatus(anyLong(), any()))
            .thenReturn(true);
        when(seatRepository.findMinPriceByConcertId(anyLong()))
            .thenReturn(99.99);

        // When
        ConcertPageDTO result = service.getAllConcerts(
            null, "berlin", null, null, "date", "asc", 0, 20
        );

        // Then
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).venue()).contains("Berlin");
    }

    @Test
    @DisplayName("Sollte nach Preis-Range filtern (Post-Filter)")
    void shouldFilterByPriceRange() {
        // Given
        Concert concert1 = createConcert(1L, "The Weeknd", LocalDateTime.of(2026, 11, 1, 20, 0), "Berlin Arena");
        Concert concert2 = createConcert(2L, "Coldplay", LocalDateTime.of(2026, 11, 3, 19, 0), "Hamburg Stadium");

        Page<Concert> mockPage = new PageImpl<>(List.of(concert1, concert2));

        when(concertRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(mockPage);

        // Concert 1: 99.99€
        when(seatRepository.existsByConcertIdAndStatus(1L, SeatStatus.AVAILABLE))
            .thenReturn(true);
        when(seatRepository.findMinPriceByConcertId(1L))
            .thenReturn(99.99);

        // Concert 2: 79.99€
        when(seatRepository.existsByConcertIdAndStatus(2L, SeatStatus.AVAILABLE))
            .thenReturn(true);
        when(seatRepository.findMinPriceByConcertId(2L))
            .thenReturn(79.99);

        // When - Filter 70-90€
        ConcertPageDTO result = service.getAllConcerts(
            null, null, 70.0, 90.0, "date", "asc", 0, 20
        );

        // Then - Nur Concert 2 (79.99€) sollte durchkommen
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).name()).isEqualTo("Coldplay");
        assertThat(result.items().get(0).minPrice()).isEqualTo(79.99);
    }

    @Test
    @DisplayName("Sollte Concerts ohne Seats (kein minPrice) durchlassen")
    void shouldIncludeConcertsWithoutSeats() {
        // Given
        Concert concert = createConcert(1L, "The Weeknd", LocalDateTime.of(2026, 11, 1, 20, 0), "Berlin Arena");

        Page<Concert> mockPage = new PageImpl<>(List.of(concert));

        when(concertRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(mockPage);
        when(seatRepository.existsByConcertIdAndStatus(1L, SeatStatus.AVAILABLE))
            .thenReturn(false);
        when(seatRepository.findMinPriceByConcertId(1L))
            .thenReturn(null); // Keine Seats

        // When
        ConcertPageDTO result = service.getAllConcerts(
            null, null, 50.0, 100.0, "date", "asc", 0, 20
        );

        // Then - Concert ohne Seats sollte durchkommen (trotz Preis-Filter)
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).minPrice()).isNull();
        assertThat(result.items().get(0).available()).isFalse();
    }

    @Test
    @DisplayName("Sollte nach Name sortieren")
    void shouldSortByName() {
        // Given
        Page<Concert> mockPage = new PageImpl<>(List.of());

        when(concertRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(mockPage);

        // When
        service.getAllConcerts(null, null, null, null, "name", "asc", 0, 20);

        // Then - Verify Sort by "name"
        verify(concertRepository).findAll(
            any(Specification.class),
            argThat(pageable ->
                pageable.getSort().getOrderFor("name") != null
            )
        );
    }

    @Test
    @DisplayName("Sollte Default-Sortierung (date) verwenden")
    void shouldUseDefaultSorting() {
        // Given
        Page<Concert> mockPage = new PageImpl<>(List.of());

        when(concertRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(mockPage);

        // When - kein sortBy
        service.getAllConcerts(null, null, null, null, null, "asc", 0, 20);

        // Then - Verify Sort by "date"
        verify(concertRepository).findAll(
            any(Specification.class),
            argThat(pageable ->
                pageable.getSort().getOrderFor("date") != null
            )
        );
    }

    // ==================== TEST HELPERS ====================

    private Concert createConcert(Long id, String name, LocalDateTime date, String venue) {
        Concert concert = Concert.createConcert(name, date, venue, null);
        // Set ID via reflection (private field)
        try {
            var idField = Concert.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(concert, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return concert;
    }
}
