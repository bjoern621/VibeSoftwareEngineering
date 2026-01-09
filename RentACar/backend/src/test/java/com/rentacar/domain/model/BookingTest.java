package com.rentacar.domain.model;

import com.rentacar.domain.exception.BookingStatusTransitionException;
import com.rentacar.domain.exception.CancellationDeadlineExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests für Booking Entity.
 * Testet Business-Logik und Invarianten gemäß DDD-Prinzipien.
 */
class BookingTest {

    private Customer customer;
    private Vehicle vehicle;
    private Branch pickupBranch;
    private Branch returnBranch;
    private LocalDateTime pickupDateTime;
    private LocalDateTime returnDateTime;
    private BigDecimal totalPrice;

    @BeforeEach
    void setUp() {
        // Mock-Objekte für Tests erstellen (Reihenfolge wichtig!)
        pickupBranch = new Branch("Filiale Hamburg", "Hamburg Hauptbahnhof 1", "Mo-Fr 8-18 Uhr");
        returnBranch = new Branch("Filiale Berlin", "Berlin Mitte 2", "Mo-So 7-20 Uhr");
        customer = createTestCustomer();
        vehicle = createTestVehicle();
        pickupDateTime = LocalDateTime.now().plusDays(7);
        returnDateTime = LocalDateTime.now().plusDays(10);
        totalPrice = new BigDecimal("299.99");
    }

    @Test
    @DisplayName("Neue Buchung sollte Status REQUESTED haben")
    void newBooking_shouldHaveRequestedStatus() {
        Booking booking = new Booking(customer, vehicle, pickupBranch, returnBranch,
                pickupDateTime, returnDateTime, totalPrice, null);

        assertEquals(BookingStatus.REQUESTED, booking.getStatus());
        assertNotNull(booking.getCreatedAt());
    }

    @Test
    @DisplayName("Buchung im Status REQUESTED kann bestätigt werden")
    void requestedBooking_canBeConfirmed() {
        Booking booking = new Booking(customer, vehicle, pickupBranch, returnBranch,
                pickupDateTime, returnDateTime, totalPrice, null);

        booking.confirm();

        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
    }

    @Test
    @DisplayName("Nur REQUESTED Buchungen können bestätigt werden")
    void onlyRequestedBooking_canBeConfirmed() {
        Booking booking = new Booking(customer, vehicle, pickupBranch, returnBranch,
                pickupDateTime, returnDateTime, totalPrice, null);
        booking.confirm();

        assertThrows(BookingStatusTransitionException.class, booking::confirm);
    }

    @Test
    @DisplayName("Buchung kann innerhalb 24h Frist storniert werden")
    void booking_canBeCancelled_withinDeadline() {
        Booking booking = new Booking(customer, vehicle, pickupBranch, returnBranch,
                pickupDateTime, returnDateTime, totalPrice, null);
        LocalDateTime now = pickupDateTime.minusHours(30); // 30h vor Abholung

        booking.cancel(now, "Kundenänderung");

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertEquals("Kundenänderung", booking.getCancellationReason());
    }

    @Test
    @DisplayName("Buchung kann nicht weniger als 24h vor Abholung storniert werden")
    void booking_cannotBeCancelled_afterDeadline() {
        Booking booking = new Booking(customer, vehicle, pickupBranch, returnBranch,
                pickupDateTime, returnDateTime, totalPrice, null);
        LocalDateTime now = pickupDateTime.minusHours(20); // Nur 20h vor Abholung

        assertThrows(CancellationDeadlineExceededException.class,
                () -> booking.cancel(now, "Zu spät"));
    }

    @Test
    @DisplayName("Aktive Buchung kann abgeschlossen werden")
    void activeBooking_canBeCompleted() {
        Booking booking = new Booking(customer, vehicle, pickupBranch, returnBranch,
                pickupDateTime, returnDateTime, totalPrice, null);
        booking.confirm();
        booking.activate();

        booking.complete();

        assertEquals(BookingStatus.COMPLETED, booking.getStatus());
    }

    @Test
    @DisplayName("Nur aktive Buchung kann abgeschlossen werden")
    void onlyActiveBooking_canBeCompleted() {
        Booking booking = new Booking(customer, vehicle, pickupBranch, returnBranch,
                pickupDateTime, returnDateTime, totalPrice, null);
        booking.confirm();

        // Confirmed booking cannot be completed directly
        assertThrows(BookingStatusTransitionException.class, booking::complete);
    }

    @Test
    @DisplayName("REQUESTED oder CONFIRMED Buchung kann ablaufen")
    void requestedOrConfirmedBooking_canExpire() {
        Booking booking = new Booking(customer, vehicle, pickupBranch, returnBranch,
                pickupDateTime, returnDateTime, totalPrice, null);

        booking.expire();

        assertEquals(BookingStatus.EXPIRED, booking.getStatus());
    }

    @Test
    @DisplayName("Buchung mit Zusatzleistungen wird korrekt erstellt")
    void booking_withAdditionalServices_isCreatedCorrectly() {
        Set<AdditionalServiceType> services = new HashSet<>();
        services.add(AdditionalServiceType.GPS);
        services.add(AdditionalServiceType.CHILD_SEAT);

        Booking booking = new Booking(customer, vehicle, pickupBranch, returnBranch,
                pickupDateTime, returnDateTime, totalPrice, services);

        assertEquals(2, booking.getAdditionalServices().size());
        assertTrue(booking.getAdditionalServices().contains(AdditionalServiceType.GPS));
    }

    @Test
    @DisplayName("Zusatzleistungen sind unveränderlich")
    void additionalServices_areImmutable() {
        Set<AdditionalServiceType> services = new HashSet<>();
        services.add(AdditionalServiceType.GPS);

        Booking booking = new Booking(customer, vehicle, pickupBranch, returnBranch,
                pickupDateTime, returnDateTime, totalPrice, services);

        Set<AdditionalServiceType> returnedServices = booking.getAdditionalServices();

        assertThrows(UnsupportedOperationException.class,
                () -> returnedServices.add(AdditionalServiceType.CHILD_SEAT));
    }

    @Test
    @DisplayName("Buchung mit negativem Preis wird abgelehnt")
    void booking_withNegativePrice_isRejected() {
        BigDecimal negativePrice = new BigDecimal("-100.00");

        assertThrows(com.rentacar.domain.exception.InvalidBookingDataException.class,
                () -> new Booking(customer, vehicle, pickupBranch, returnBranch,
                        pickupDateTime, returnDateTime, negativePrice, null));
    }

    @Test
    @DisplayName("Buchung mit null Kunde wird abgelehnt")
    void booking_withNullCustomer_isRejected() {
        assertThrows(NullPointerException.class,
                () -> new Booking(null, vehicle, pickupBranch, returnBranch,
                        pickupDateTime, returnDateTime, totalPrice, null));
    }

    // Hilfsmethoden zum Erstellen von Test-Objekten

    private Customer createTestCustomer() {
        try {
            return new Customer(
                    "Max",
                    "Mustermann",
                    new Address("Teststraße 123", "12345", "Teststadt"),
                    new DriverLicenseNumber("DE123456789"),
                    "max.mustermann@test.de",
                    "Password123!"
            );
        } catch (Exception e) {
            throw new RuntimeException("Fehler beim Erstellen des Test-Kunden", e);
        }
    }

    private Vehicle createTestVehicle() {
        try {
            return new Vehicle(
                    LicensePlate.of("HH-AB 1234"),
                    "BMW",
                    "3er",
                    2023,
                    Mileage.of(15000),
                    VehicleType.SEDAN,
                    pickupBranch
            );
        } catch (Exception e) {
            throw new RuntimeException("Fehler beim Erstellen des Test-Fahrzeugs", e);
        }
    }
}
