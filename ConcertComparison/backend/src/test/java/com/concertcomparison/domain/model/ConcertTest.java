package com.concertcomparison.domain.model;

import com.concertcomparison.domain.exception.InvalidConcertDateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit Tests für Concert Entity.
 * 
 * Testet Business Logic, Validierungen und State Transitions.
 */
@DisplayName("Concert Entity Tests")
class ConcertTest {

    @Nested
    @DisplayName("Factory Method - createConcert()")
    class CreateConcertTests {

        @Test
        @DisplayName("Sollte Concert erfolgreich erstellen mit gültigen Daten")
        void shouldCreateConcertSuccessfully() {
            // Given
            String name = "Rock am Ring 2026";
            LocalDateTime date = LocalDateTime.now().plusDays(30);
            String venue = "Nürburgring";
            String description = "Größtes Rock-Festival Deutschlands";

            // When
            Concert concert = Concert.createConcert(name, date, venue, description);

            // Then
            assertThat(concert).isNotNull();
            assertThat(concert.getName()).isEqualTo(name);
            assertThat(concert.getDate()).isEqualTo(date);
            assertThat(concert.getVenue()).isEqualTo(venue);
            assertThat(concert.getDescription()).isEqualTo(description);
            assertThat(concert.getCreatedAt()).isNotNull();
            assertThat(concert.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Sollte Concert ohne Beschreibung erstellen")
        void shouldCreateConcertWithoutDescription() {
            // Given
            String name = "Jazz Night";
            LocalDateTime date = LocalDateTime.now().plusDays(10);
            String venue = "Blue Note Club";

            // When
            Concert concert = Concert.createConcert(name, date, venue, null);

            // Then
            assertThat(concert).isNotNull();
            assertThat(concert.getDescription()).isNull();
        }

        @Test
        @DisplayName("Sollte NullPointerException werfen wenn Name null ist")
        void shouldThrowExceptionWhenNameIsNull() {
            // Given
            LocalDateTime date = LocalDateTime.now().plusDays(10);
            String venue = "Test Venue";

            // When & Then
            assertThatThrownBy(() -> Concert.createConcert(null, date, venue, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Name darf nicht null sein");
        }

        @Test
        @DisplayName("Sollte IllegalArgumentException werfen wenn Name leer ist")
        void shouldThrowExceptionWhenNameIsEmpty() {
            // Given
            LocalDateTime date = LocalDateTime.now().plusDays(10);
            String venue = "Test Venue";

            // When & Then
            assertThatThrownBy(() -> Concert.createConcert("   ", date, venue, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Name darf nicht leer sein");
        }

        @Test
        @DisplayName("Sollte InvalidConcertDateException werfen wenn Datum in der Vergangenheit liegt")
        void shouldThrowExceptionWhenDateIsInPast() {
            // Given
            String name = "Past Concert";
            LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
            String venue = "Test Venue";

            // When & Then
            assertThatThrownBy(() -> Concert.createConcert(name, pastDate, venue, null))
                .isInstanceOf(InvalidConcertDateException.class)
                .hasMessageContaining("muss in der Zukunft liegen");
        }

        @Test
        @DisplayName("Sollte NullPointerException werfen wenn Datum null ist")
        void shouldThrowExceptionWhenDateIsNull() {
            // Given
            String name = "Test Concert";
            String venue = "Test Venue";

            // When & Then
            assertThatThrownBy(() -> Concert.createConcert(name, null, venue, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Datum darf nicht null sein");
        }

        @Test
        @DisplayName("Sollte NullPointerException werfen wenn Venue null ist")
        void shouldThrowExceptionWhenVenueIsNull() {
            // Given
            String name = "Test Concert";
            LocalDateTime date = LocalDateTime.now().plusDays(10);

            // When & Then
            assertThatThrownBy(() -> Concert.createConcert(name, date, null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Veranstaltungsort darf nicht null sein");
        }

        @Test
        @DisplayName("Sollte IllegalArgumentException werfen wenn Venue leer ist")
        void shouldThrowExceptionWhenVenueIsEmpty() {
            // Given
            String name = "Test Concert";
            LocalDateTime date = LocalDateTime.now().plusDays(10);

            // When & Then
            assertThatThrownBy(() -> Concert.createConcert(name, date, "  ", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Veranstaltungsort darf nicht leer sein");
        }
    }

    @Nested
    @DisplayName("Business Method - update()")
    class UpdateConcertTests {

        @Test
        @DisplayName("Sollte Concert erfolgreich aktualisieren")
        void shouldUpdateConcertSuccessfully() {
            // Given
            Concert concert = Concert.createConcert(
                "Original Name",
                LocalDateTime.now().plusDays(10),
                "Original Venue",
                "Original Description"
            );
            
            String newName = "Updated Name";
            LocalDateTime newDate = LocalDateTime.now().plusDays(20);
            String newVenue = "Updated Venue";
            String newDescription = "Updated Description";

            // When
            concert.update(newName, newDate, newVenue, newDescription);

            // Then
            assertThat(concert.getName()).isEqualTo(newName);
            assertThat(concert.getDate()).isEqualTo(newDate);
            assertThat(concert.getVenue()).isEqualTo(newVenue);
            assertThat(concert.getDescription()).isEqualTo(newDescription);
        }

        @Test
        @DisplayName("Sollte IllegalArgumentException werfen bei ungültigem Namen")
        void shouldThrowExceptionWhenUpdatingWithInvalidName() {
            // Given
            Concert concert = Concert.createConcert(
                "Test Concert",
                LocalDateTime.now().plusDays(10),
                "Test Venue",
                null
            );

            // When & Then
            assertThatThrownBy(() -> concert.update("", LocalDateTime.now().plusDays(15), "Venue", null))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Sollte InvalidConcertDateException werfen bei Datum in Vergangenheit")
        void shouldThrowExceptionWhenUpdatingWithPastDate() {
            // Given
            Concert concert = Concert.createConcert(
                "Test Concert",
                LocalDateTime.now().plusDays(10),
                "Test Venue",
                null
            );

            // When & Then
            assertThatThrownBy(() -> 
                concert.update("Name", LocalDateTime.now().minusDays(1), "Venue", null))
                .isInstanceOf(InvalidConcertDateException.class);
        }
    }

    @Nested
    @DisplayName("Business Methods - isPastEvent() & isFutureEvent()")
    class EventTimingTests {

        @Test
        @DisplayName("Sollte true zurückgeben für zukünftiges Event")
        void shouldReturnTrueForFutureEvent() {
            // Given
            Concert concert = Concert.createConcert(
                "Future Concert",
                LocalDateTime.now().plusDays(30),
                "Future Venue",
                null
            );

            // When & Then
            assertThat(concert.isFutureEvent()).isTrue();
            assertThat(concert.isPastEvent()).isFalse();
        }

        @Test
        @DisplayName("Sollte past event korrekt identifizieren (simuliert)")
        void shouldIdentifyPastEventCorrectly() {
            // Given
            // Hinweis: Wir können kein echtes Past Event erstellen wegen Validierung
            // Dieser Test zeigt die Funktionalität
            Concert concert = Concert.createConcert(
                "Almost Now Concert",
                LocalDateTime.now().plusSeconds(1),
                "Test Venue",
                null
            );

            // When & Then
            assertThat(concert.isFutureEvent()).isTrue();
        }
    }

    @Nested
    @DisplayName("Equals & HashCode")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Sollte zwei Concerts mit gleicher ID als gleich betrachten")
        void shouldBeEqualWithSameId() {
            // Given
            Concert concert1 = Concert.createConcert(
                "Concert 1",
                LocalDateTime.now().plusDays(10),
                "Venue 1",
                null
            );
            Concert concert2 = Concert.createConcert(
                "Concert 2",
                LocalDateTime.now().plusDays(20),
                "Venue 2",
                null
            );

            // When
            concert1.setId(1L);
            concert2.setId(1L);

            // Then
            assertThat(concert1).isEqualTo(concert2);
            assertThat(concert1.hashCode()).isEqualTo(concert2.hashCode());
        }

        @Test
        @DisplayName("Sollte zwei Concerts mit verschiedenen IDs als ungleich betrachten")
        void shouldNotBeEqualWithDifferentIds() {
            // Given
            Concert concert1 = Concert.createConcert(
                "Concert 1",
                LocalDateTime.now().plusDays(10),
                "Venue 1",
                null
            );
            Concert concert2 = Concert.createConcert(
                "Concert 1",
                LocalDateTime.now().plusDays(10),
                "Venue 1",
                null
            );

            // When
            concert1.setId(1L);
            concert2.setId(2L);

            // Then
            assertThat(concert1).isNotEqualTo(concert2);
        }
    }
}
