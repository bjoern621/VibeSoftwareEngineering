package com.rentacar.application.service;

import com.rentacar.domain.model.*;
import com.rentacar.domain.repository.DamageReportRepository;
import com.rentacar.domain.repository.RentalAgreementRepository;
import com.rentacar.domain.repository.VehicleRepository;
import com.rentacar.domain.service.EmailService;
import com.rentacar.presentation.dto.CreateDamageReportRequestDTO;
import com.rentacar.presentation.dto.DamageReportResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DamageReportApplicationServiceTest {

    @Mock
    private DamageReportRepository damageReportRepository;

    @Mock
    private RentalAgreementRepository rentalAgreementRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private DamageReportApplicationService service;

    private RentalAgreement rentalAgreement;
    private Booking booking;
    private Vehicle vehicle;
    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = mock(Customer.class);
        when(customer.getEmail()).thenReturn("test@example.com");
        when(customer.getFirstName()).thenReturn("John");
        when(customer.getLastName()).thenReturn("Doe");

        vehicle = mock(Vehicle.class);
        when(vehicle.getId()).thenReturn(50L);

        booking = mock(Booking.class);
        when(booking.getCustomer()).thenReturn(customer);
        when(booking.getVehicle()).thenReturn(vehicle);
        when(booking.getId()).thenReturn(100L);

        rentalAgreement = mock(RentalAgreement.class);
        when(rentalAgreement.getBooking()).thenReturn(booking);
        when(rentalAgreement.getId()).thenReturn(1L);
    }

    @Test
    void createDamageReport_Success() {
        Long bookingId = 100L;
        CreateDamageReportRequestDTO request = new CreateDamageReportRequestDTO();
        request.setDescription("Scratch on bumper");
        request.setEstimatedCost(new BigDecimal("150.00"));
        request.setPhotos(List.of("url1", "url2"));

        when(rentalAgreementRepository.findByBookingId(bookingId)).thenReturn(Optional.of(rentalAgreement));
        when(damageReportRepository.save(any(DamageReport.class))).thenAnswer(invocation -> {
            DamageReport report = invocation.getArgument(0);
            // We can't set ID on the entity easily without reflection or a setter, 
            // but for this test we just check the return object which is mapped from the entity.
            // The DTO mapping uses report.getId() which will be null.
            return report;
        });

        DamageReportResponseDTO response = service.createDamageReport(bookingId, request);

        assertNotNull(response);
        assertEquals("Scratch on bumper", response.getDescription());
        assertEquals(new BigDecimal("150.00"), response.getEstimatedCost());
        
        verify(vehicle).markAsInMaintenance();
        verify(vehicleRepository).save(vehicle);
        verify(emailService).sendDamageReportNotification(eq("test@example.com"), anyString(), eq("Scratch on bumper"));
    }

    @Test
    void createDamageReport_BookingNotFound() {
        Long bookingId = 999L;
        CreateDamageReportRequestDTO request = new CreateDamageReportRequestDTO();
        
        when(rentalAgreementRepository.findByBookingId(bookingId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.createDamageReport(bookingId, request));
    }
}
