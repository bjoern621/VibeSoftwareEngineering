package com.rentacar.domain.service;

import com.rentacar.domain.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdditionalCostServiceTest {

    private final AdditionalCostService service = new AdditionalCostService();

    @Test
    void calculateAdditionalCosts_NoCosts() {
        Booking booking = mock(Booking.class);
        RentalAgreement agreement = mock(RentalAgreement.class);
        
        when(booking.getReturnDateTime()).thenReturn(LocalDateTime.now().plusDays(1));
        when(booking.getIncludedKilometers()).thenReturn(500);
        
        when(agreement.getCheckinTime()).thenReturn(LocalDateTime.now());
        when(agreement.getCheckoutMileage()).thenReturn(Mileage.of(1000));
        when(agreement.getCheckinMileage()).thenReturn(Mileage.of(1200)); // 200km driven

        AdditionalCosts costs = service.calculateAdditionalCosts(booking, agreement, Collections.emptyList());

        assertEquals(BigDecimal.ZERO, costs.getTotalAdditionalCost());
    }

    @Test
    void calculateAdditionalCosts_LateFee() {
        Booking booking = mock(Booking.class);
        RentalAgreement agreement = mock(RentalAgreement.class);
        
        LocalDateTime returnDate = LocalDateTime.now().minusHours(2);
        when(booking.getReturnDateTime()).thenReturn(returnDate);
        when(booking.getIncludedKilometers()).thenReturn(500);
        
        when(agreement.getCheckinTime()).thenReturn(LocalDateTime.now()); // 2 hours late
        when(agreement.getCheckoutMileage()).thenReturn(Mileage.of(1000));
        when(agreement.getCheckinMileage()).thenReturn(Mileage.of(1200));

        AdditionalCosts costs = service.calculateAdditionalCosts(booking, agreement, Collections.emptyList());

        // 2 hours * 20 = 40
        assertEquals(new BigDecimal("40.00"), costs.getLateFee());
    }

    @Test
    void calculateAdditionalCosts_ExcessMileage() {
        Booking booking = mock(Booking.class);
        RentalAgreement agreement = mock(RentalAgreement.class);
        
        when(booking.getReturnDateTime()).thenReturn(LocalDateTime.now().plusDays(1));
        when(booking.getIncludedKilometers()).thenReturn(100);
        
        when(agreement.getCheckinTime()).thenReturn(LocalDateTime.now());
        when(agreement.getCheckoutMileage()).thenReturn(Mileage.of(1000));
        when(agreement.getCheckinMileage()).thenReturn(Mileage.of(1200)); // 200km driven, 100 excess

        AdditionalCosts costs = service.calculateAdditionalCosts(booking, agreement, Collections.emptyList());

        // 100 * 0.30 = 30.00
        assertEquals(new BigDecimal("30.00"), costs.getExcessMileageFee());
    }

    @Test
    void calculateAdditionalCosts_Damage() {
        Booking booking = mock(Booking.class);
        RentalAgreement agreement = mock(RentalAgreement.class);
        
        when(booking.getReturnDateTime()).thenReturn(LocalDateTime.now().plusDays(1));
        when(booking.getIncludedKilometers()).thenReturn(500);
        
        when(agreement.getCheckinTime()).thenReturn(LocalDateTime.now());
        when(agreement.getCheckoutMileage()).thenReturn(Mileage.of(1000));
        when(agreement.getCheckinMileage()).thenReturn(Mileage.of(1200));

        DamageReport report = mock(DamageReport.class);
        when(report.getEstimatedCost()).thenReturn(new BigDecimal("150.00"));

        AdditionalCosts costs = service.calculateAdditionalCosts(booking, agreement, List.of(report));

        assertEquals(new BigDecimal("150.00"), costs.getDamageCost());
    }
}
