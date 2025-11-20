package com.rentacar.domain.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests für Mileage Value Object.
 */
class MileageTest {
    
    @Test
    void shouldCreateValidMileage() {
        // Given
        Integer kilometers = 50000;
        
        // When
        Mileage mileage = Mileage.of(kilometers);
        
        // Then
        assertNotNull(mileage);
        assertEquals(50000, mileage.getKilometers());
    }
    
    @Test
    void shouldCreateZeroMileage() {
        // When
        Mileage mileage = Mileage.zero();
        
        // Then
        assertNotNull(mileage);
        assertEquals(0, mileage.getKilometers());
    }
    
    @Test
    void shouldAcceptZeroKilometers() {
        // When & Then
        assertDoesNotThrow(() -> Mileage.of(0));
    }
    
    @Test
    void shouldRejectNullKilometers() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
            () -> Mileage.of(null));
    }
    
    @Test
    void shouldRejectNegativeKilometers() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
            () -> Mileage.of(-100));
        assertThrows(IllegalArgumentException.class, 
            () -> Mileage.of(-1));
    }
    
    @Test
    void shouldAddKilometers() {
        // Given
        Mileage original = Mileage.of(50000);
        
        // When
        Mileage updated = original.add(1000);
        
        // Then
        assertEquals(51000, updated.getKilometers());
        assertEquals(50000, original.getKilometers()); // Original unveränderlich
    }
    
    @Test
    void shouldRejectNegativeAddition() {
        // Given
        Mileage mileage = Mileage.of(50000);
        
        // When & Then
        assertThrows(IllegalArgumentException.class, 
            () -> mileage.add(-100));
    }
    
    @Test
    void shouldCompareGreaterThan() {
        // Given
        Mileage higher = Mileage.of(60000);
        Mileage lower = Mileage.of(50000);
        
        // Then
        assertTrue(higher.isGreaterThan(lower));
        assertFalse(lower.isGreaterThan(higher));
        assertFalse(higher.isGreaterThan(higher));
    }
    
    @Test
    void shouldCompareLessThan() {
        // Given
        Mileage higher = Mileage.of(60000);
        Mileage lower = Mileage.of(50000);
        
        // Then
        assertTrue(lower.isLessThan(higher));
        assertFalse(higher.isLessThan(lower));
        assertFalse(lower.isLessThan(lower));
    }
    
    @Test
    void shouldBeEqualWhenKilometersAreEqual() {
        // Given
        Mileage mileage1 = Mileage.of(50000);
        Mileage mileage2 = Mileage.of(50000);
        
        // Then
        assertEquals(mileage1, mileage2);
        assertEquals(mileage1.hashCode(), mileage2.hashCode());
    }
    
    @Test
    void shouldNotBeEqualWhenKilometersAreDifferent() {
        // Given
        Mileage mileage1 = Mileage.of(50000);
        Mileage mileage2 = Mileage.of(60000);
        
        // Then
        assertNotEquals(mileage1, mileage2);
    }
    
    @Test
    void shouldHaveCorrectStringRepresentation() {
        // Given
        Mileage mileage = Mileage.of(50000);
        
        // Then
        assertEquals("50000 km", mileage.toString());
    }
}
