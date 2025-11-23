package com.rentacar.domain.service;

import com.rentacar.domain.exception.VehicleNotAvailableException;
import com.rentacar.domain.model.*;
import com.rentacar.domain.repository.BookingRepository;
import com.rentacar.domain.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingDomainServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    private BookingDomainService bookingDomainService;

    private Customer customer;
    private Vehicle vehicle;
    private Branch branch;
    private LocalDateTime pickupTime;
    private LocalDateTime returnTime;

    @BeforeEach
    void setUp() {
        bookingDomainService = new BookingDomainService(bookingRepository, vehicleRepository);

        customer = mock(Customer.class);
        vehicle = mock(Vehicle.class);
        branch = mock(Branch.class);
        
        pickupTime = LocalDateTime.now().plusDays(1);
        returnTime = LocalDateTime.now().plusDays(3);

        lenient().when(vehicle.getId()).thenReturn(1L);
        when(vehicle.getVehicleType()).thenReturn(VehicleType.COMPACT_CAR);
    }

    @Test
    @DisplayName("AC1: Bei Buchungserstellung wird geprüft, ob Fahrzeug im Zeitraum verfügbar ist (Erfolg)")
    void createBooking_Success() {
        // Arrange
        when(vehicle.getStatus()).thenReturn(VehicleStatus.AVAILABLE);
        when(bookingRepository.findOverlappingBookings(any(), any(), any())).thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Booking booking = bookingDomainService.createBooking(customer, vehicle, branch, branch, pickupTime, returnTime, new HashSet<>());

        // Assert
        assertNotNull(booking);
        verify(bookingRepository).findOverlappingBookings(1L, pickupTime, returnTime);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    @DisplayName("AC2: Überlappende Buchungen werden verhindert")
    void createBooking_OverlappingBooking_ThrowsException() {
        // Arrange
        when(vehicle.getStatus()).thenReturn(VehicleStatus.AVAILABLE);
        Booking existingBooking = mock(Booking.class);
        when(bookingRepository.findOverlappingBookings(any(), any(), any())).thenReturn(List.of(existingBooking));
        
        Vehicle alternative = mock(Vehicle.class);
        when(vehicleRepository.findAvailableVehicles(any(), any(), any())).thenReturn(List.of(alternative));

        // Act & Assert
        VehicleNotAvailableException exception = assertThrows(VehicleNotAvailableException.class, () -> {
            bookingDomainService.createBooking(customer, vehicle, branch, branch, pickupTime, returnTime, new HashSet<>());
        });

        assertEquals("Fahrzeug ist im gewählten Zeitraum bereits gebucht.", exception.getMessage());
        assertFalse(exception.getAlternativeVehicles().isEmpty());
    }

    @Test
    @DisplayName("AC3: Fahrzeuge mit Status IN_WARTUNG können nicht gebucht werden")
    void createBooking_VehicleInMaintenance_ThrowsException() {
        // Arrange
        when(vehicle.getStatus()).thenReturn(VehicleStatus.IN_MAINTENANCE);
        
        Vehicle alternative = mock(Vehicle.class);
        when(vehicleRepository.findAvailableVehicles(any(), any(), any())).thenReturn(List.of(alternative));

        // Act & Assert
        VehicleNotAvailableException exception = assertThrows(VehicleNotAvailableException.class, () -> {
            bookingDomainService.createBooking(customer, vehicle, branch, branch, pickupTime, returnTime, new HashSet<>());
        });

        assertTrue(exception.getMessage().contains("Status: IN_MAINTENANCE"));
        assertFalse(exception.getAlternativeVehicles().isEmpty());
    }

    @Test
    @DisplayName("AC3: Fahrzeuge mit Status AUSSER_BETRIEB können nicht gebucht werden")
    void createBooking_VehicleOutOfService_ThrowsException() {
        // Arrange
        when(vehicle.getStatus()).thenReturn(VehicleStatus.OUT_OF_SERVICE);
        
        Vehicle alternative = mock(Vehicle.class);
        when(vehicleRepository.findAvailableVehicles(any(), any(), any())).thenReturn(List.of(alternative));

        // Act & Assert
        VehicleNotAvailableException exception = assertThrows(VehicleNotAvailableException.class, () -> {
            bookingDomainService.createBooking(customer, vehicle, branch, branch, pickupTime, returnTime, new HashSet<>());
        });

        assertTrue(exception.getMessage().contains("Status: OUT_OF_SERVICE"));
        assertFalse(exception.getAlternativeVehicles().isEmpty());
    }
}
