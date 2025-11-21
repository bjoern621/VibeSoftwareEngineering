package com.rentacar.domain.service;

import com.rentacar.domain.model.AdditionalServiceType;
import com.rentacar.domain.model.DateRange;
import com.rentacar.domain.model.PricingCalculation;
import com.rentacar.domain.model.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests für PricingService Domain Service.
 */
class PricingServiceTest {
    
    private PricingService pricingService;
    
    @BeforeEach
    void setUp() {
        pricingService = new PricingService();
    }
    
    @Test
    void shouldCalculateBasePriceOnly() {
        // Arrange
        VehicleType vehicleType = VehicleType.COMPACT_CAR; // 29.99 EUR/Tag
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(3); // 3 Tage
        DateRange rentalPeriod = new DateRange(start, end);
        List<AdditionalServiceType> noServices = Collections.emptyList();
        
        // Act
        PricingCalculation result = pricingService.calculatePrice(
            vehicleType, 
            rentalPeriod, 
            noServices
        );
        
        // Assert
        assertEquals(VehicleType.COMPACT_CAR, result.getVehicleType());
        assertEquals(3, result.getNumberOfDays());
        assertEquals(new BigDecimal("89.97"), result.getBasePrice()); // 29.99 × 3
        assertEquals(BigDecimal.ZERO, result.getAdditionalServicesPrice());
        assertEquals(new BigDecimal("89.97"), result.getTotalPrice());
        assertTrue(result.getAdditionalServices().isEmpty());
    }
    
    @Test
    void shouldCalculatePriceWithAdditionalServices() {
        // Arrange
        VehicleType vehicleType = VehicleType.SEDAN; // 49.99 EUR/Tag
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(2); // 2 Tage
        DateRange rentalPeriod = new DateRange(start, end);
        List<AdditionalServiceType> services = Arrays.asList(
            AdditionalServiceType.GPS,            // 8.00 EUR/Tag
            AdditionalServiceType.CHILD_SEAT      // 5.00 EUR/Tag
        );
        
        // Act
        PricingCalculation result = pricingService.calculatePrice(
            vehicleType, 
            rentalPeriod, 
            services
        );
        
        // Assert
        assertEquals(VehicleType.SEDAN, result.getVehicleType());
        assertEquals(2, result.getNumberOfDays());
        assertEquals(new BigDecimal("99.98"), result.getBasePrice()); // 49.99 × 2
        assertEquals(new BigDecimal("26.00"), result.getAdditionalServicesPrice()); // (8 + 5) × 2
        assertEquals(new BigDecimal("125.98"), result.getTotalPrice()); // 99.98 + 26.00
        assertEquals(2, result.getAdditionalServices().size());
    }
    
    @Test
    void shouldCalculatePriceForPremiumVehicle() {
        // Arrange
        VehicleType vehicleType = VehicleType.SUV; // 79.99 EUR/Tag
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(5); // 5 Tage
        DateRange rentalPeriod = new DateRange(start, end);
        List<AdditionalServiceType> services = Arrays.asList(
            AdditionalServiceType.FULL_INSURANCE,      // 15.00 EUR/Tag
            AdditionalServiceType.ADDITIONAL_DRIVER    // 12.00 EUR/Tag
        );
        
        // Act
        PricingCalculation result = pricingService.calculatePrice(
            vehicleType, 
            rentalPeriod, 
            services
        );
        
        // Assert
        assertEquals(VehicleType.SUV, result.getVehicleType());
        assertEquals(5, result.getNumberOfDays());
        assertEquals(new BigDecimal("399.95"), result.getBasePrice()); // 79.99 × 5
        assertEquals(new BigDecimal("135.00"), result.getAdditionalServicesPrice()); // (15 + 12) × 5
        assertEquals(new BigDecimal("534.95"), result.getTotalPrice());
    }
    
    @Test
    void shouldCalculatePriceForOneDayRental() {
        // Arrange
        VehicleType vehicleType = VehicleType.VAN; // 69.99 EUR/Tag
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(6); // Weniger als 1 Tag, wird zu 1 Tag aufgerundet
        DateRange rentalPeriod = new DateRange(start, end);
        List<AdditionalServiceType> noServices = Collections.emptyList();
        
        // Act
        PricingCalculation result = pricingService.calculatePrice(
            vehicleType, 
            rentalPeriod, 
            noServices
        );
        
        // Assert
        assertEquals(1, result.getNumberOfDays());
        assertEquals(new BigDecimal("69.99"), result.getBasePrice());
        assertEquals(new BigDecimal("69.99"), result.getTotalPrice());
    }
    
    @Test
    void shouldCalculateBasePriceMethod() {
        // Arrange
        VehicleType vehicleType = VehicleType.COMPACT_CAR;
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(4);
        DateRange rentalPeriod = new DateRange(start, end);
        
        // Act
        BigDecimal basePrice = pricingService.calculateBasePrice(vehicleType, rentalPeriod);
        
        // Assert
        assertEquals(new BigDecimal("119.96"), basePrice); // 29.99 × 4
    }
    
    @Test
    void shouldCalculateAdditionalServicesPriceMethod() {
        // Arrange
        List<AdditionalServiceType> services = Arrays.asList(
            AdditionalServiceType.GPS,
            AdditionalServiceType.WINTER_TIRES
        );
        int numberOfDays = 3;
        
        // Act
        BigDecimal servicesPrice = pricingService.calculateAdditionalServicesPrice(
            services, 
            numberOfDays
        );
        
        // Assert
        assertEquals(new BigDecimal("42.00"), servicesPrice); // (8.00 + 6.00) × 3
    }
    
    @Test
    void shouldThrowExceptionWhenVehicleTypeIsNull() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(2);
        DateRange rentalPeriod = new DateRange(start, end);
        
        // Act & Assert
        assertThrows(
            NullPointerException.class,
            () -> pricingService.calculatePrice(null, rentalPeriod, Collections.emptyList())
        );
    }
    
    @Test
    void shouldThrowExceptionWhenRentalPeriodIsNull() {
        // Arrange
        VehicleType vehicleType = VehicleType.SEDAN;
        
        // Act & Assert
        assertThrows(
            NullPointerException.class,
            () -> pricingService.calculatePrice(vehicleType, null, Collections.emptyList())
        );
    }
    
    @Test
    void shouldThrowExceptionWhenAdditionalServicesIsNull() {
        // Arrange
        VehicleType vehicleType = VehicleType.SEDAN;
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(2);
        DateRange rentalPeriod = new DateRange(start, end);
        
        // Act & Assert
        assertThrows(
            NullPointerException.class,
            () -> pricingService.calculatePrice(vehicleType, rentalPeriod, null)
        );
    }
    
    @Test
    void shouldThrowExceptionForInvalidNumberOfDays() {
        // Arrange
        List<AdditionalServiceType> services = Arrays.asList(AdditionalServiceType.GPS);
        
        // Act & Assert
        assertThrows(
            IllegalArgumentException.class,
            () -> pricingService.calculateAdditionalServicesPrice(services, 0)
        );
    }
    
    @Test
    void shouldHandleAllVehicleTypes() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(1);
        DateRange rentalPeriod = new DateRange(start, end);
        
        // Act & Assert - alle Fahrzeugtypen sollten funktionieren
        for (VehicleType vehicleType : VehicleType.values()) {
            PricingCalculation result = pricingService.calculatePrice(
                vehicleType, 
                rentalPeriod, 
                Collections.emptyList()
            );
            assertNotNull(result);
            assertTrue(result.getTotalPrice().compareTo(BigDecimal.ZERO) > 0);
        }
    }
    
    @Test
    void shouldHandleAllAdditionalServices() {
        // Arrange
        VehicleType vehicleType = VehicleType.SEDAN;
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(1);
        DateRange rentalPeriod = new DateRange(start, end);
        
        // Act & Assert - alle Zusatzleistungen sollten funktionieren
        for (AdditionalServiceType service : AdditionalServiceType.values()) {
            PricingCalculation result = pricingService.calculatePrice(
                vehicleType, 
                rentalPeriod, 
                Arrays.asList(service)
            );
            assertNotNull(result);
            assertEquals(1, result.getAdditionalServices().size());
            assertTrue(result.getAdditionalServicesPrice().compareTo(BigDecimal.ZERO) > 0);
        }
    }
}
