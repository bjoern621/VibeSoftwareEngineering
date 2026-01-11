package com.concertcomparison.domain.model;

import com.concertcomparison.domain.exception.SeatNotAvailableException;
import com.concertcomparison.domain.exception.SeatNotHeldException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit Tests für Seat Entity.
 * 
 * Testet Business Methods, State Transitions und Invarianten.
 */
@DisplayName("Seat Entity Unit Tests")
class SeatTest {
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Erstellt Seat mit gültigen Parametern")
        void createSeat_WithValidParameters_Success() {
            // Act
            Seat seat = new Seat(1L, "A-12", "VIP", "Block A");
            
            // Assert
            assertThat(seat.getConcertId()).isEqualTo(1L);
            assertThat(seat.getSeatNumber()).isEqualTo("A-12");
            assertThat(seat.getCategory()).isEqualTo("VIP");
            assertThat(seat.getBlock()).isEqualTo("Block A");
            assertThat(seat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
            assertThat(seat.isAvailable()).isTrue();
        }
        
        @Test
        @DisplayName("Wirft Exception bei null Concert-ID")
        void createSeat_WithNullConcertId_ThrowsException() {
            // Act & Assert
            assertThatThrownBy(() -> new Seat(null, "A-12", "VIP", "Block A"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Concert-ID darf nicht null sein");
        }
        
        @Test
        @DisplayName("Wirft Exception bei leerer Sitzplatznummer")
        void createSeat_WithEmptySeatNumber_ThrowsException() {
            // Act & Assert
            assertThatThrownBy(() -> new Seat(1L, "", "VIP", "Block A"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sitzplatznummer darf nicht leer sein");
        }
        
        @Test
        @DisplayName("Wirft Exception bei leerer Kategorie")
        void createSeat_WithEmptyCategory_ThrowsException() {
            // Act & Assert
            assertThatThrownBy(() -> new Seat(1L, "A-12", "", "Block A"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Kategorie darf nicht leer sein");
        }
    }
    
    @Nested
    @DisplayName("hold() Method Tests")
    class HoldTests {
        
        @Test
        @DisplayName("hold() - Reserviert verfügbaren Seat erfolgreich")
        void hold_AvailableSeat_Success() {
            // Arrange
            Seat seat = new Seat(1L, "A-12", "VIP", "Block A");
            String reservationId = "RES-123";
            int holdDurationMinutes = 15;
            
            // Act
            seat.hold(reservationId, holdDurationMinutes);
            
            // Assert
            assertThat(seat.getStatus()).isEqualTo(SeatStatus.HELD);
            assertThat(seat.isHeld()).isTrue();
            assertThat(seat.isAvailable()).isFalse();
            assertThat(seat.getHoldReservationId()).isEqualTo(reservationId);
            assertThat(seat.getHoldExpiresAt()).isNotNull();
            assertThat(seat.getHoldExpiresAt()).isAfter(LocalDateTime.now());
        }
        
        @Test
        @DisplayName("hold() - Wirft Exception bei bereits reserviertem Seat")
        void hold_AlreadyHeldSeat_ThrowsException() {
            // Arrange
            Seat seat = new Seat(1L, "A-12", "VIP", "Block A");
            seat.hold("RES-123", 15);
            
            // Act & Assert
            assertThatThrownBy(() -> seat.hold("RES-456", 15))
                .isInstanceOf(SeatNotAvailableException.class)
                .hasMessageContaining("bereits Reserviert");
        }
        
        @Test
        @DisplayName("hold() - Wirft Exception bei verkauftem Seat")
        void hold_SoldSeat_ThrowsException() {
            // Arrange
            Seat seat = new Seat(1L, "A-12", "VIP", "Block A");
            seat.hold("RES-123", 15);
            seat.sell();
            
            // Act & Assert
            assertThatThrownBy(() -> seat.hold("RES-456", 15))
                .isInstanceOf(SeatNotAvailableException.class)
                .hasMessageContaining("bereits Verkauft");
        }
        
        @Test
        @DisplayName("hold() - Wirft Exception bei leerer Reservation-ID")
        void hold_WithEmptyReservationId_ThrowsException() {
            // Arrange
            Seat seat = new Seat(1L, "A-12", "VIP", "Block A");
            
            // Act & Assert
            assertThatThrownBy(() -> seat.hold("", 15))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Reservation-ID darf nicht leer sein");
        }
        
        @Test
        @DisplayName("hold() - Wirft Exception bei negativer Hold-Dauer")
        void hold_WithNegativeDuration_ThrowsException() {
            // Arrange
            Seat seat = new Seat(1L, "A-12", "VIP", "Block A");
            
            // Act & Assert
            assertThatThrownBy(() -> seat.hold("RES-123", -5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Hold-Dauer muss positiv sein");
        }
    }
    
    @Nested
    @DisplayName("sell() Method Tests")
    class SellTests {
        
        @Test
        @DisplayName("sell() - Verkauft reservierten Seat erfolgreich")
        void sell_HeldSeat_Success() {
            // Arrange
            Seat seat = new Seat(1L, "A-12", "VIP", "Block A");
            seat.hold("RES-123", 15);
            
            // Act
            seat.sell();
            
            // Assert
            assertThat(seat.getStatus()).isEqualTo(SeatStatus.SOLD);
            assertThat(seat.isSold()).isTrue();
            assertThat(seat.isHeld()).isFalse();
            assertThat(seat.isAvailable()).isFalse();
            assertThat(seat.getHoldReservationId()).isNull();
            assertThat(seat.getHoldExpiresAt()).isNull();
        }
        
        @Test
        @DisplayName("sell() - Wirft Exception bei nicht-reserviertem Seat")
        void sell_NotHeldSeat_ThrowsException() {
            // Arrange
            Seat seat = new Seat(1L, "A-12", "VIP", "Block A");
            
            // Act & Assert
            assertThatThrownBy(() -> seat.sell())
                .isInstanceOf(SeatNotHeldException.class)
                .hasMessageContaining("muss zuerst reserviert werden");
        }
        
        @Test
        @DisplayName("sell() - Wirft Exception bei bereits verkauftem Seat")
        void sell_AlreadySoldSeat_ThrowsException() {
            // Arrange
            Seat seat = new Seat(1L, "A-12", "VIP", "Block A");
            seat.hold("RES-123", 15);
            seat.sell();
            
            // Act & Assert
            assertThatThrownBy(() -> seat.sell())
                .isInstanceOf(SeatNotHeldException.class);
        }
    }
    
    @Nested
    @DisplayName("releaseHold() Method Tests")
    class ReleaseHoldTests {
        
        @Test
        @DisplayName("releaseHold() - Gibt reservierten Seat frei")
        void releaseHold_HeldSeat_Success() {
            // Arrange
            Seat seat = new Seat(1L, "A-12", "VIP", "Block A");
            seat.hold("RES-123", 15);
            
            // Act
            seat.releaseHold();
            
            // Assert
            assertThat(seat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
            assertThat(seat.isAvailable()).isTrue();
            assertThat(seat.isHeld()).isFalse();
            assertThat(seat.getHoldReservationId()).isNull();
            assertThat(seat.getHoldExpiresAt()).isNull();
        }
        
        @Test
        @DisplayName("releaseHold() - Wirft Exception bei verfügbarem Seat")
        void releaseHold_AvailableSeat_ThrowsException() {
            // Arrange
            Seat seat = new Seat(1L, "A-12", "VIP", "Block A");
            
            // Act & Assert
            assertThatThrownBy(() -> seat.releaseHold())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nicht reserviert");
        }
        
        @Test
        @DisplayName("releaseHold() - Wirft Exception bei verkauftem Seat")
        void releaseHold_SoldSeat_ThrowsException() {
            // Arrange
            Seat seat = new Seat(1L, "A-12", "VIP", "Block A");
            seat.hold("RES-123", 15);
            seat.sell();
            
            // Act & Assert
            assertThatThrownBy(() -> seat.releaseHold())
                .isInstanceOf(IllegalStateException.class);
        }
    }
    
    @Nested
    @DisplayName("isHoldExpired() Method Tests")
    class IsHoldExpiredTests {
        
        @Test
        @DisplayName("isHoldExpired() - Gibt false für nicht-abgelaufene Hold zurück")
        void isHoldExpired_NotExpired_ReturnsFalse() {
            // Arrange
            Seat seat = new Seat(1L, "A-12", "VIP", "Block A");
            seat.hold("RES-123", 60); // 60 Minuten Hold
            
            // Act & Assert
            assertThat(seat.isHoldExpired()).isFalse();
        }
        
        @Test
        @DisplayName("isHoldExpired() - Gibt false für verfügbaren Seat zurück")
        void isHoldExpired_AvailableSeat_ReturnsFalse() {
            // Arrange
            Seat seat = new Seat(1L, "A-12", "VIP", "Block A");
            
            // Act & Assert
            assertThat(seat.isHoldExpired()).isFalse();
        }
        
        @Test
        @DisplayName("isHoldExpired() - Gibt false für verkauften Seat zurück")
        void isHoldExpired_SoldSeat_ReturnsFalse() {
            // Arrange
            Seat seat = new Seat(1L, "A-12", "VIP", "Block A");
            seat.hold("RES-123", 15);
            seat.sell();
            
            // Act & Assert
            assertThat(seat.isHoldExpired()).isFalse();
        }
    }
    
    @Nested
    @DisplayName("State Transition Tests")
    class StateTransitionTests {
        
        @Test
        @DisplayName("Vollständiger Lebenszyklus: AVAILABLE → HELD → SOLD")
        void fullLifecycle_AvailableToHeldToSold_Success() {
            // Arrange
            Seat seat = new Seat(1L, "A-12", "VIP", "Block A");
            
            // Act & Assert: AVAILABLE → HELD
            assertThat(seat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
            seat.hold("RES-123", 15);
            assertThat(seat.getStatus()).isEqualTo(SeatStatus.HELD);
            
            // Act & Assert: HELD → SOLD
            seat.sell();
            assertThat(seat.getStatus()).isEqualTo(SeatStatus.SOLD);
        }
        
        @Test
        @DisplayName("Stornierungszyklus: AVAILABLE → HELD → AVAILABLE")
        void cancellationCycle_AvailableToHeldToAvailable_Success() {
            // Arrange
            Seat seat = new Seat(1L, "A-12", "VIP", "Block A");
            
            // Act & Assert: AVAILABLE → HELD
            assertThat(seat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
            seat.hold("RES-123", 15);
            assertThat(seat.getStatus()).isEqualTo(SeatStatus.HELD);
            
            // Act & Assert: HELD → AVAILABLE
            seat.releaseHold();
            assertThat(seat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
        }
        
        @Test
        @DisplayName("SOLD ist finaler Zustand (keine Transition mehr möglich)")
        void soldIsFinalState_NoTransitionPossible() {
            // Arrange
            Seat seat = new Seat(1L, "A-12", "VIP", "Block A");
            seat.hold("RES-123", 15);
            seat.sell();
            
            // Assert: Keine Transitionen mehr möglich
            assertThatThrownBy(() -> seat.hold("RES-456", 15))
                .isInstanceOf(SeatNotAvailableException.class);
            
            assertThatThrownBy(() -> seat.releaseHold())
                .isInstanceOf(IllegalStateException.class);
        }
    }
}
