package com.rentacar.domain.model;

import com.rentacar.domain.exception.InvalidLicensePlateException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests für LicensePlate Value Object.
 */
class LicensePlateTest {
    
    @Test
    void shouldCreateValidLicensePlate() {
        // Given
        String validPlate = "B-AB 1234";
        
        // When
        LicensePlate licensePlate = LicensePlate.of(validPlate);
        
        // Then
        assertNotNull(licensePlate);
        assertEquals("B-AB 1234", licensePlate.getValue());
    }
    
    @Test
    void shouldNormalizeLicensePlateToUpperCase() {
        // Given
        String lowerCasePlate = "b-ab 1234";
        
        // When
        LicensePlate licensePlate = LicensePlate.of(lowerCasePlate);
        
        // Then
        assertEquals("B-AB 1234", licensePlate.getValue());
    }
    
    @Test
    void shouldAcceptVariousValidFormats() {
        // Valid formats
        assertDoesNotThrow(() -> LicensePlate.of("B-AB 1234"));
        assertDoesNotThrow(() -> LicensePlate.of("HH-XY 99"));
        assertDoesNotThrow(() -> LicensePlate.of("M-A 1"));
        assertDoesNotThrow(() -> LicensePlate.of("BGL-AB 1234"));
        assertDoesNotThrow(() -> LicensePlate.of("HH-AB 1234H")); // E-Kennzeichen
    }
    
    @Test
    void shouldRejectNullLicensePlate() {
        // When & Then
        assertThrows(InvalidLicensePlateException.class, 
            () -> LicensePlate.of(null));
    }
    
    @Test
    void shouldRejectEmptyLicensePlate() {
        // When & Then
        assertThrows(InvalidLicensePlateException.class, 
            () -> LicensePlate.of(""));
        assertThrows(InvalidLicensePlateException.class, 
            () -> LicensePlate.of("   "));
    }
    
    @Test
    void shouldRejectInvalidFormats() {
        // Invalid formats
        assertThrows(InvalidLicensePlateException.class, 
            () -> LicensePlate.of("123456")); // nur Zahlen
        assertThrows(InvalidLicensePlateException.class, 
            () -> LicensePlate.of("ABCDEF")); // nur Buchstaben
        assertThrows(InvalidLicensePlateException.class, 
            () -> LicensePlate.of("B AB 1234")); // fehlender Bindestrich
        assertThrows(InvalidLicensePlateException.class, 
            () -> LicensePlate.of("B-AB-1234")); // zu viele Bindestriche
    }
    
    @Test
    void shouldBeEqualWhenValueIsEqual() {
        // Given
        LicensePlate plate1 = LicensePlate.of("B-AB 1234");
        LicensePlate plate2 = LicensePlate.of("B-AB 1234");
        
        // Then
        assertEquals(plate1, plate2);
        assertEquals(plate1.hashCode(), plate2.hashCode());
    }
    
    @Test
    void shouldNotBeEqualWhenValueIsDifferent() {
        // Given
        LicensePlate plate1 = LicensePlate.of("B-AB 1234");
        LicensePlate plate2 = LicensePlate.of("HH-XY 99");
        
        // Then
        assertNotEquals(plate1, plate2);
    }
    
    @Test
    void shouldHaveCorrectStringRepresentation() {
        // Given
        LicensePlate licensePlate = LicensePlate.of("B-AB 1234");
        
        // Then
        assertEquals("B-AB 1234", licensePlate.toString());
    }
}
