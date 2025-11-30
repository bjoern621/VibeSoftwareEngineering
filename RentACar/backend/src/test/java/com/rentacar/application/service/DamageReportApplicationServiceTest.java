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
import java.util.Arrays;
import java.util.Collections;
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

    @Test
    void getDamageReportsByBooking_Success() {
        Long bookingId = 100L;
        
        DamageReport report1 = mock(DamageReport.class);
        when(report1.getId()).thenReturn(1L);
        when(report1.getDescription()).thenReturn("Kratzer");
        when(report1.getEstimatedCost()).thenReturn(new BigDecimal("100.00"));
        when(report1.getPhotos()).thenReturn(Collections.emptyList());
        when(report1.getRentalAgreement()).thenReturn(rentalAgreement);
        
        DamageReport report2 = mock(DamageReport.class);
        when(report2.getId()).thenReturn(2L);
        when(report2.getDescription()).thenReturn("Delle");
        when(report2.getEstimatedCost()).thenReturn(new BigDecimal("200.00"));
        when(report2.getPhotos()).thenReturn(Collections.emptyList());
        when(report2.getRentalAgreement()).thenReturn(rentalAgreement);
        
        when(rentalAgreementRepository.findByBookingId(bookingId)).thenReturn(Optional.of(rentalAgreement));
        when(damageReportRepository.findByRentalAgreementId(1L)).thenReturn(Arrays.asList(report1, report2));
        
        List<DamageReportResponseDTO> result = service.getDamageReportsByBooking(bookingId);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Kratzer", result.get(0).getDescription());
        assertEquals("Delle", result.get(1).getDescription());
    }

    @Test
    void getDamageReportsByBooking_NoRentalAgreement_ReturnsEmptyList() {
        Long bookingId = 999L;
        
        when(rentalAgreementRepository.findByBookingId(bookingId)).thenReturn(Optional.empty());
        
        List<DamageReportResponseDTO> result = service.getDamageReportsByBooking(bookingId);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getDamageReportsByBooking_NoDamageReports_ReturnsEmptyList() {
        Long bookingId = 100L;
        
        when(rentalAgreementRepository.findByBookingId(bookingId)).thenReturn(Optional.of(rentalAgreement));
        when(damageReportRepository.findByRentalAgreementId(1L)).thenReturn(Collections.emptyList());
        
        List<DamageReportResponseDTO> result = service.getDamageReportsByBooking(bookingId);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getDamageReport_Success() {
        Long reportId = 1L;
        
        DamageReport report = mock(DamageReport.class);
        when(report.getId()).thenReturn(reportId);
        when(report.getDescription()).thenReturn("Test Schaden");
        when(report.getEstimatedCost()).thenReturn(new BigDecimal("150.00"));
        when(report.getPhotos()).thenReturn(List.of("photo1.jpg"));
        when(report.getRentalAgreement()).thenReturn(rentalAgreement);
        
        when(damageReportRepository.findById(reportId)).thenReturn(Optional.of(report));
        
        DamageReportResponseDTO result = service.getDamageReport(reportId);
        
        assertNotNull(result);
        assertEquals(reportId, result.getId());
        assertEquals("Test Schaden", result.getDescription());
        assertEquals(new BigDecimal("150.00"), result.getEstimatedCost());
    }

    @Test
    void getDamageReport_NotFound_ThrowsException() {
        Long reportId = 999L;
        
        when(damageReportRepository.findById(reportId)).thenReturn(Optional.empty());
        
        assertThrows(IllegalArgumentException.class, () -> service.getDamageReport(reportId));
    }
}
