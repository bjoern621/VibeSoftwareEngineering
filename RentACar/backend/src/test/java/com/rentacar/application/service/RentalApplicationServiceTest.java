package com.rentacar.application.service;

import com.rentacar.domain.model.*;
import com.rentacar.domain.repository.BookingRepository;
import com.rentacar.domain.repository.RentalAgreementRepository;
import com.rentacar.domain.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalApplicationServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private RentalAgreementRepository rentalAgreementRepository;
    @Mock
    private VehicleRepository vehicleRepository;

    private RentalApplicationService rentalApplicationService;

    @BeforeEach
    void setUp() {
        rentalApplicationService = new RentalApplicationService(bookingRepository, rentalAgreementRepository, vehicleRepository);
    }

    @Test
    void performCheckOut_Success() {
        // Arrange
        Long bookingId = 1L;
        Booking booking = mock(Booking.class);
        Vehicle vehicle = mock(Vehicle.class);
        
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(booking.getStatus()).thenReturn(BookingStatus.CONFIRMED);
        when(booking.getVehicle()).thenReturn(vehicle);
        
        // Act
        rentalApplicationService.performCheckOut(bookingId, 1000, "1/1", "CLEAN", "None");
        
        // Assert
        verify(vehicle).markAsRented();
        verify(vehicleRepository).save(vehicle);
        verify(booking).activate();
        verify(bookingRepository).save(booking);
        
        ArgumentCaptor<RentalAgreement> captor = ArgumentCaptor.forClass(RentalAgreement.class);
        verify(rentalAgreementRepository).save(captor.capture());
        RentalAgreement savedAgreement = captor.getValue();
        
        assertEquals(booking, savedAgreement.getBooking());
        assertEquals(1000, savedAgreement.getCheckoutMileage().getKilometers());
        assertEquals("1/1", savedAgreement.getCheckoutCondition().getFuelLevel());
        assertEquals("CLEAN", savedAgreement.getCheckoutCondition().getCleanliness());
        assertEquals("None", savedAgreement.getCheckoutCondition().getDamagesDescription());
        assertEquals(RentalAgreementStatus.OPEN, savedAgreement.getStatus());
    }

    @Test
    void performCheckOut_BookingNotFound() {
        // Arrange
        Long bookingId = 1L;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            rentalApplicationService.performCheckOut(bookingId, 1000, "1/1", "CLEAN", "None")
        );
    }

    @Test
    void performCheckOut_BookingNotConfirmed() {
        // Arrange
        Long bookingId = 1L;
        Booking booking = mock(Booking.class);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(booking.getStatus()).thenReturn(BookingStatus.REQUESTED);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> 
            rentalApplicationService.performCheckOut(bookingId, 1000, "1/1", "CLEAN", "None")
        );
    }
}
