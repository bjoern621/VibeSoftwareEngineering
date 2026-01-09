package com.rentacar.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests für VehicleType Enum.
 * 
 * Testet die Geschäftslogik und Eigenschaften der Fahrzeugtypen.
 */
class VehicleTypeTest {
    
    @Test
    void shouldHaveAllFourVehicleTypes() {
        // Given & When
        List<VehicleType> allTypes = VehicleType.getAllTypes();
        
        // Then
        assertEquals(4, allTypes.size(), "Es sollten genau 4 Fahrzeugtypen existieren");
        assertTrue(allTypes.contains(VehicleType.COMPACT_CAR));
        assertTrue(allTypes.contains(VehicleType.SEDAN));
        assertTrue(allTypes.contains(VehicleType.SUV));
        assertTrue(allTypes.contains(VehicleType.VAN));
    }
    
    @Test
    void compactCarShouldHaveCorrectProperties() {
        // Given
        VehicleType compactCar = VehicleType.COMPACT_CAR;
        
        // Then
        assertEquals("Kleinwagen", compactCar.getDisplayName());
        assertEquals("Kompaktklasse", compactCar.getCategory());
        assertEquals(PriceClass.ECONOMY, compactCar.getPriceClass());
        assertEquals(new BigDecimal("29.99"), compactCar.getDailyBaseRate());
        assertEquals(5, compactCar.getPassengerCapacity());
    }
    
    @Test
    void sedanShouldHaveCorrectProperties() {
        // Given
        VehicleType sedan = VehicleType.SEDAN;
        
        // Then
        assertEquals("Limousine", sedan.getDisplayName());
        assertEquals("Mittelklasse", sedan.getCategory());
        assertEquals(PriceClass.STANDARD, sedan.getPriceClass());
        assertEquals(new BigDecimal("49.99"), sedan.getDailyBaseRate());
        assertEquals(5, sedan.getPassengerCapacity());
    }
    
    @Test
    void suvShouldHaveCorrectProperties() {
        // Given
        VehicleType suv = VehicleType.SUV;
        
        // Then
        assertEquals("SUV", suv.getDisplayName());
        assertEquals("Geländewagen", suv.getCategory());
        assertEquals(PriceClass.PREMIUM, suv.getPriceClass());
        assertEquals(new BigDecimal("79.99"), suv.getDailyBaseRate());
        assertEquals(7, suv.getPassengerCapacity());
    }
    
    @Test
    void vanShouldHaveCorrectProperties() {
        // Given
        VehicleType van = VehicleType.VAN;
        
        // Then
        assertEquals("Transporter", van.getDisplayName());
        assertEquals("Nutzfahrzeug", van.getCategory());
        assertEquals(PriceClass.STANDARD, van.getPriceClass());
        assertEquals(new BigDecimal("69.99"), van.getDailyBaseRate());
        assertEquals(9, van.getPassengerCapacity());
    }
    
    @Test
    void fromStringShouldFindExistingVehicleType() {
        // Given
        String typeName = "COMPACT_CAR";
        
        // When
        Optional<VehicleType> result = VehicleType.fromString(typeName);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(VehicleType.COMPACT_CAR, result.get());
    }
    
    @Test
    void fromStringShouldBeCaseInsensitive() {
        // Given
        String lowerCase = "compact_car";
        String mixedCase = "CoMpAcT_CaR";
        
        // When
        Optional<VehicleType> lowerResult = VehicleType.fromString(lowerCase);
        Optional<VehicleType> mixedResult = VehicleType.fromString(mixedCase);
        
        // Then
        assertTrue(lowerResult.isPresent());
        assertTrue(mixedResult.isPresent());
        assertEquals(VehicleType.COMPACT_CAR, lowerResult.get());
        assertEquals(VehicleType.COMPACT_CAR, mixedResult.get());
    }
    
    @Test
    void fromStringShouldReturnEmptyForInvalidType() {
        // Given
        String invalidType = "INVALID_TYPE";
        
        // When
        Optional<VehicleType> result = VehicleType.fromString(invalidType);
        
        // Then
        assertFalse(result.isPresent());
    }
    
    @Test
    void fromStringShouldReturnEmptyForNullInput() {
        // When
        Optional<VehicleType> result = VehicleType.fromString(null);
        
        // Then
        assertFalse(result.isPresent());
    }
    
    @Test
    void fromStringShouldReturnEmptyForBlankInput() {
        // Given
        String blankInput = "   ";
        
        // When
        Optional<VehicleType> result = VehicleType.fromString(blankInput);
        
        // Then
        assertFalse(result.isPresent());
    }
    
    @Test
    void getAllTypesShouldReturnAllFourTypes() {
        // When
        List<VehicleType> allTypes = VehicleType.getAllTypes();
        
        // Then
        assertNotNull(allTypes);
        assertEquals(4, allTypes.size());
    }
}
