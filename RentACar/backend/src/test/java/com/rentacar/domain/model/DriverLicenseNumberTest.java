package com.rentacar.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests für DriverLicenseNumber Value Object.
 */
class DriverLicenseNumberTest {

    @Test
    void shouldCreateValidDriverLicenseNumber() {
        // Given
        String validNumber = "B123456789X";

        // When
        DriverLicenseNumber licenseNumber = new DriverLicenseNumber(validNumber);

        // Then
        assertNotNull(licenseNumber);
        assertEquals("B123456789X", licenseNumber.getNumber());
    }

    @Test
    void shouldNormalizeToUpperCase() {
        // Given
        String lowerCaseNumber = "b123456789x";

        // When
        DriverLicenseNumber licenseNumber = new DriverLicenseNumber(lowerCaseNumber);

        // Then
        assertEquals("B123456789X", licenseNumber.getNumber());
    }

    @Test
    void shouldAcceptNumericCharacters() {
        // Given
        String numericNumber = "12345678901";

        // When
        DriverLicenseNumber licenseNumber = new DriverLicenseNumber(numericNumber);

        // Then
        assertEquals("12345678901", licenseNumber.getNumber());
    }

    @Test
    void shouldRejectNullNumber() {
        // When & Then
        com.rentacar.domain.exception.InvalidDriverLicenseException exception = assertThrows(
            com.rentacar.domain.exception.InvalidDriverLicenseException.class,
            () -> new DriverLicenseNumber(null)
        );
        assertTrue(exception.getMessage().contains("Führerscheinnummer"));
        assertTrue(exception.getMessage().contains("leer"));
    }

    @Test
    void shouldRejectEmptyNumber() {
        // When & Then
        com.rentacar.domain.exception.InvalidDriverLicenseException exception = assertThrows(
            com.rentacar.domain.exception.InvalidDriverLicenseException.class,
            () -> new DriverLicenseNumber("   ")
        );
        assertTrue(exception.getMessage().contains("Führerscheinnummer"));
        assertTrue(exception.getMessage().contains("leer"));
    }

    @Test
    void shouldRejectTooShortNumber() {
        // Given
        String shortNumber = "B12345678";

        // When & Then
        com.rentacar.domain.exception.InvalidDriverLicenseException exception = assertThrows(
            com.rentacar.domain.exception.InvalidDriverLicenseException.class,
            () -> new DriverLicenseNumber(shortNumber)
        );
        assertTrue(exception.getMessage().contains("11 Zeichen"));
    }

    @Test
    void shouldRejectTooLongNumber() {
        // Given
        String longNumber = "B123456789XY";

        // When & Then
        com.rentacar.domain.exception.InvalidDriverLicenseException exception = assertThrows(
            com.rentacar.domain.exception.InvalidDriverLicenseException.class,
            () -> new DriverLicenseNumber(longNumber)
        );
        assertTrue(exception.getMessage().contains("11 Zeichen"));
    }

    @Test
    void shouldRejectInvalidCharacters() {
        // Given
        String invalidNumber = "B12345678!@";

        // When & Then
        com.rentacar.domain.exception.InvalidDriverLicenseException exception = assertThrows(
            com.rentacar.domain.exception.InvalidDriverLicenseException.class,
            () -> new DriverLicenseNumber(invalidNumber)
        );
        assertTrue(exception.getMessage().contains("Buchstaben und Ziffern"));
    }

    @Test
    void shouldRejectNumberWithSpaces() {
        // Given
        String numberWithSpaces = "B12 345 678";

        // When & Then
        assertThrows(
            com.rentacar.domain.exception.InvalidDriverLicenseException.class,
            () -> new DriverLicenseNumber(numberWithSpaces)
        );
    }

    @Test
    void shouldImplementEqualsCorrectly() {
        // Given
        DriverLicenseNumber license1 = new DriverLicenseNumber("B123456789X");
        DriverLicenseNumber license2 = new DriverLicenseNumber("B123456789X");
        DriverLicenseNumber license3 = new DriverLicenseNumber("C987654321Y");

        // Then
        assertEquals(license1, license2);
        assertNotEquals(license1, license3);
        assertEquals(license1, license1);
        assertNotEquals(license1, null);
    }

    @Test
    void shouldImplementHashCodeCorrectly() {
        // Given
        DriverLicenseNumber license1 = new DriverLicenseNumber("B123456789X");
        DriverLicenseNumber license2 = new DriverLicenseNumber("B123456789X");

        // Then
        assertEquals(license1.hashCode(), license2.hashCode());
    }

    @Test
    void shouldImplementToStringCorrectly() {
        // Given
        DriverLicenseNumber licenseNumber = new DriverLicenseNumber("B123456789X");

        // When
        String result = licenseNumber.toString();

        // Then
        assertEquals("B123456789X", result);
    }

    @Test
    void shouldTreatCaseInsensitiveNumbersAsEqual() {
        // Given
        DriverLicenseNumber license1 = new DriverLicenseNumber("b123456789x");
        DriverLicenseNumber license2 = new DriverLicenseNumber("B123456789X");

        // Then
        assertEquals(license1, license2);
        assertEquals(license1.hashCode(), license2.hashCode());
    }
}
