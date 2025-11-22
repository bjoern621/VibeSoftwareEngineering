package com.rentacar.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests für Address Value Object.
 */
class AddressTest {

    @Test
    void shouldCreateValidAddress() {
        // Given
        String street = "Hauptstraße 123";
        String postalCode = "12345";
        String city = "Berlin";

        // When
        Address address = new Address(street, postalCode, city);

        // Then
        assertNotNull(address);
        assertEquals(street, address.getStreet());
        assertEquals(postalCode, address.getPostalCode());
        assertEquals(city, address.getCity());
    }

    @Test
    void shouldRejectNullStreet() {
        // When & Then
        com.rentacar.domain.exception.InvalidAddressException exception = assertThrows(
            com.rentacar.domain.exception.InvalidAddressException.class,
            () -> new Address(null, "12345", "Berlin")
        );
        assertTrue(exception.getMessage().contains("Straße"));
    }

    @Test
    void shouldRejectEmptyStreet() {
        // When & Then
        com.rentacar.domain.exception.InvalidAddressException exception = assertThrows(
            com.rentacar.domain.exception.InvalidAddressException.class,
            () -> new Address("  ", "12345", "Berlin")
        );
        assertTrue(exception.getMessage().contains("Straße"));
    }

    @Test
    void shouldRejectTooShortStreet() {
        // When & Then
        com.rentacar.domain.exception.InvalidAddressException exception = assertThrows(
            com.rentacar.domain.exception.InvalidAddressException.class,
            () -> new Address("AB", "12345", "Berlin")
        );
        assertTrue(exception.getMessage().contains("zwischen 3 und 100"));
    }

    @Test
    void shouldRejectTooLongStreet() {
        // Given
        String longStreet = "A".repeat(101);

        // When & Then
        com.rentacar.domain.exception.InvalidAddressException exception = assertThrows(
            com.rentacar.domain.exception.InvalidAddressException.class,
            () -> new Address(longStreet, "12345", "Berlin")
        );
        assertTrue(exception.getMessage().contains("zwischen 3 und 100"));
    }

    @Test
    void shouldRejectNullPostalCode() {
        // When & Then
        com.rentacar.domain.exception.InvalidAddressException exception = assertThrows(
            com.rentacar.domain.exception.InvalidAddressException.class,
            () -> new Address("Hauptstraße 123", null, "Berlin")
        );
        assertTrue(exception.getMessage().contains("Postleitzahl"));
    }

    @Test
    void shouldRejectInvalidPostalCodeFormat() {
        // When & Then
        com.rentacar.domain.exception.InvalidAddressException exception = assertThrows(
            com.rentacar.domain.exception.InvalidAddressException.class,
            () -> new Address("Hauptstraße 123", "1234", "Berlin")
        );
        assertTrue(exception.getMessage().contains("5 Ziffern"));
    }

    @Test
    void shouldRejectPostalCodeWithLetters() {
        // When & Then
        com.rentacar.domain.exception.InvalidAddressException exception = assertThrows(
            com.rentacar.domain.exception.InvalidAddressException.class,
            () -> new Address("Hauptstraße 123", "1234A", "Berlin")
        );
        assertTrue(exception.getMessage().contains("5 Ziffern"));
    }

    @Test
    void shouldRejectNullCity() {
        // When & Then
        com.rentacar.domain.exception.InvalidAddressException exception = assertThrows(
            com.rentacar.domain.exception.InvalidAddressException.class,
            () -> new Address("Hauptstraße 123", "12345", null)
        );
        assertTrue(exception.getMessage().contains("Stadt"));
    }

    @Test
    void shouldRejectEmptyCity() {
        // When & Then
        com.rentacar.domain.exception.InvalidAddressException exception = assertThrows(
            com.rentacar.domain.exception.InvalidAddressException.class,
            () -> new Address("Hauptstraße 123", "12345", "  ")
        );
        assertTrue(exception.getMessage().contains("Stadt"));
    }

    @Test
    void shouldRejectTooShortCity() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Address("Hauptstraße 123", "12345", "A")
        );
        assertTrue(exception.getMessage().contains("zwischen 2 und 100"));
    }

    @Test
    void shouldImplementEqualsCorrectly() {
        // Given
        Address address1 = new Address("Hauptstraße 123", "12345", "Berlin");
        Address address2 = new Address("Hauptstraße 123", "12345", "Berlin");
        Address address3 = new Address("Nebenstraße 456", "54321", "München");

        // Then
        assertEquals(address1, address2);
        assertNotEquals(address1, address3);
        assertEquals(address1, address1);
        assertNotEquals(address1, null);
    }

    @Test
    void shouldImplementHashCodeCorrectly() {
        // Given
        Address address1 = new Address("Hauptstraße 123", "12345", "Berlin");
        Address address2 = new Address("Hauptstraße 123", "12345", "Berlin");

        // Then
        assertEquals(address1.hashCode(), address2.hashCode());
    }

    @Test
    void shouldImplementToStringCorrectly() {
        // Given
        Address address = new Address("Hauptstraße 123", "12345", "Berlin");

        // When
        String result = address.toString();

        // Then
        assertTrue(result.contains("Hauptstraße 123"));
        assertTrue(result.contains("12345"));
        assertTrue(result.contains("Berlin"));
    }
}
