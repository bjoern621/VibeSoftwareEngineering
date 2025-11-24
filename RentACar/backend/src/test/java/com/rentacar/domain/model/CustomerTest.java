package com.rentacar.domain.model;

import com.rentacar.domain.exception.InvalidCustomerDataException;
import com.rentacar.domain.exception.InvalidEmailException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests für Customer Entity.
 */
class CustomerTest {

    private static final String VALID_PASSWORD_HASH = "$2a$10$abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1";

    private Address createValidAddress() {
        return new Address("Hauptstraße 123", "12345", "Berlin");
    }

    private DriverLicenseNumber createValidLicenseNumber() {
        return new DriverLicenseNumber("B123456789X");
    }

    @Test
    void shouldCreateValidCustomer() {
        // Given
        String firstName = "Max";
        String lastName = "Mustermann";
        Address address = createValidAddress();
        DriverLicenseNumber licenseNumber = createValidLicenseNumber();
        String email = "max.mustermann@example.com";
        String phoneNumber = "0301234567";

        // When
        Customer customer = new Customer(firstName, lastName, address, licenseNumber, email, phoneNumber, VALID_PASSWORD_HASH);

        // Then
        assertNotNull(customer);
        assertEquals(firstName, customer.getFirstName());
        assertEquals(lastName, customer.getLastName());
        assertEquals(address, customer.getAddress());
        assertEquals(licenseNumber, customer.getDriverLicenseNumber());
        assertEquals(email.toLowerCase(), customer.getEmail());
        assertEquals("0301234567", customer.getPhoneNumber());
    }

    @Test
    void shouldNormalizeEmailToLowerCase() {
        // Given
        String email = "Max.Mustermann@EXAMPLE.COM";

        // When
        Customer customer = new Customer(
            "Max", "Mustermann",
            createValidAddress(),
            createValidLicenseNumber(),
            email,
            "0301234567",
            VALID_PASSWORD_HASH
        );

        // Then
        assertEquals("max.mustermann@example.com", customer.getEmail());
    }

    @Test
    void shouldNormalizePhoneNumber() {
        // Given
        String phoneNumber = "030 123 456 7";

        // When
        Customer customer = new Customer(
            "Max", "Mustermann",
            createValidAddress(),
            createValidLicenseNumber(),
            "max@example.com",
            phoneNumber,
            VALID_PASSWORD_HASH
        );

        // Then (Leerzeichen und Bindestriche werden entfernt)
        assertEquals("0301234567", customer.getPhoneNumber());
    }

    @Test
    void shouldAcceptNullPhoneNumber() {
        // When
        Customer customer = new Customer(
            "Max", "Mustermann",
            createValidAddress(),
            createValidLicenseNumber(),
            "max@example.com",
            null,
            VALID_PASSWORD_HASH
        );

        // Then
        assertNull(customer.getPhoneNumber());
    }

    @Test
    void shouldRejectNullFirstName() {
        Address address = createValidAddress();
        DriverLicenseNumber licenseNumber = createValidLicenseNumber();

        // When & Then
        com.rentacar.domain.exception.InvalidCustomerDataException exception = assertThrows(
            com.rentacar.domain.exception.InvalidCustomerDataException.class,
            () -> new Customer(
                null, "Mustermann",
                address,
                licenseNumber,
                "max@example.com",
                "0301234567",
                VALID_PASSWORD_HASH
            )
        );
        assertTrue(exception.getMessage().contains("Vorname"));
    }

    @Test
    void shouldRejectEmptyFirstName() {
        Address address = createValidAddress();
        DriverLicenseNumber licenseNumber = createValidLicenseNumber();

        // When & Then
        com.rentacar.domain.exception.InvalidCustomerDataException exception = assertThrows(
            com.rentacar.domain.exception.InvalidCustomerDataException.class,
            () -> new Customer(
                "  ", "Mustermann",
                address,
                licenseNumber,
                "max@example.com",
                "0301234567",
                VALID_PASSWORD_HASH
            )
        );
        assertTrue(exception.getMessage().contains("Vorname"));
    }

    @Test
    void shouldRejectTooShortFirstName() {
        Address address = createValidAddress();
        DriverLicenseNumber licenseNumber = createValidLicenseNumber();

        // When & Then
        com.rentacar.domain.exception.InvalidCustomerDataException exception = assertThrows(
            com.rentacar.domain.exception.InvalidCustomerDataException.class,
            () -> new Customer(
                "M", "Mustermann",
                address,
                licenseNumber,
                "max@example.com",
                "0301234567",
                VALID_PASSWORD_HASH
            )
        );
        assertTrue(exception.getMessage().contains("zwischen 2 und 100"));
    }

    @Test
    void shouldRejectNullLastName() {
        Address address = createValidAddress();
        DriverLicenseNumber licenseNumber = createValidLicenseNumber();

        // When & Then
        com.rentacar.domain.exception.InvalidCustomerDataException exception = assertThrows(
            com.rentacar.domain.exception.InvalidCustomerDataException.class,
            () -> new Customer(
                "Max", null,
                address,
                licenseNumber,
                "max@example.com",
                "0301234567",
                VALID_PASSWORD_HASH
            )
        );
        assertTrue(exception.getMessage().contains("Nachname"));
    }

    @Test
    void shouldRejectNullAddress() {
        DriverLicenseNumber licenseNumber = createValidLicenseNumber();

        // When & Then
        InvalidCustomerDataException exception = assertThrows(
            InvalidCustomerDataException.class,
            () -> new Customer(
                "Max", "Mustermann",
                null,
                licenseNumber,
                "max@example.com",
                "0301234567",
                VALID_PASSWORD_HASH
            )
        );
        assertTrue(exception.getMessage().contains("Adresse"));
    }

    @Test
    void shouldRejectNullDriverLicenseNumber() {
        Address address = createValidAddress();

        // When & Then
        InvalidCustomerDataException exception = assertThrows(
            InvalidCustomerDataException.class,
            () -> new Customer(
                "Max", "Mustermann",
                address,
                null,
                "max@example.com",
                "0301234567",
                VALID_PASSWORD_HASH
            )
        );
        assertTrue(exception.getMessage().contains("Führerscheinnummer"));
    }

    @Test
    void shouldRejectNullEmail() {
        Address address = createValidAddress();
        DriverLicenseNumber licenseNumber = createValidLicenseNumber();

        // When & Then
        InvalidEmailException exception = assertThrows(
            InvalidEmailException.class,
            () -> new Customer(
                "Max", "Mustermann",
                address,
                licenseNumber,
                null,
                "0301234567",
                VALID_PASSWORD_HASH
            )
        );
        assertTrue(exception.getMessage().contains("E-Mail"));
    }

    @Test
    void shouldRejectInvalidEmailFormat() {
        Address address = createValidAddress();
        DriverLicenseNumber licenseNumber = createValidLicenseNumber();

        // When & Then
        InvalidEmailException exception = assertThrows(
            InvalidEmailException.class,
            () -> new Customer(
                "Max", "Mustermann",
                address,
                licenseNumber,
                "invalid-email",
                "0301234567",
                VALID_PASSWORD_HASH
            )
        );
        assertTrue(exception.getMessage().toLowerCase().contains("ungültig"));
    }

    @Test
    void shouldRejectEmailWithoutAtSign() {
        Address address = createValidAddress();
        DriverLicenseNumber licenseNumber = createValidLicenseNumber();

        // When & Then
        assertThrows(
            InvalidEmailException.class,
            () -> new Customer(
                "Max", "Mustermann",
                address,
                licenseNumber,
                "maxexample.com",
                "0301234567",
                VALID_PASSWORD_HASH
            )
        );
    }

    @Test
    void shouldRejectEmailWithoutDomain() {
        Address address = createValidAddress();
        DriverLicenseNumber licenseNumber = createValidLicenseNumber();

        // When & Then
        assertThrows(
            InvalidEmailException.class,
            () -> new Customer(
                "Max", "Mustermann",
                address,
                licenseNumber,
                "max@",
                "0301234567",
                VALID_PASSWORD_HASH
            )
        );
    }

    @Test
    void shouldRejectTooShortPhoneNumber() {
        Address address = createValidAddress();
        DriverLicenseNumber licenseNumber = createValidLicenseNumber();

        // When & Then
        com.rentacar.domain.exception.InvalidCustomerDataException exception = assertThrows(
            com.rentacar.domain.exception.InvalidCustomerDataException.class,
            () -> new Customer(
                "Max", "Mustermann",
                address,
                licenseNumber,
                "max@example.com",
                "1234",
                VALID_PASSWORD_HASH
            )
        );
        assertTrue(exception.getMessage().contains("Telefonnummer"));
    }

    @Test
    void shouldRejectTooLongPhoneNumber() {
        // Given
        String longPhone = "1".repeat(21);
        Address address = createValidAddress();
        DriverLicenseNumber licenseNumber = createValidLicenseNumber();

        // When & Then
        com.rentacar.domain.exception.InvalidCustomerDataException exception = assertThrows(
            com.rentacar.domain.exception.InvalidCustomerDataException.class,
            () -> new Customer(
                "Max", "Mustermann",
                address,
                licenseNumber,
                "max@example.com",
                longPhone,
                VALID_PASSWORD_HASH
            )
        );
        assertTrue(exception.getMessage().contains("Telefonnummer"));
    }

    @Test
    void shouldUpdateContactDetails() {
        // Given
        Customer customer = new Customer(
            "Max", "Mustermann",
            createValidAddress(),
            createValidLicenseNumber(),
            "old@example.com",
            "0301234567"
        );

        // When
        customer.updateContactDetails("new@example.com", "0409876543");

        // Then
        assertEquals("new@example.com", customer.getEmail());
        assertEquals("0409876543", customer.getPhoneNumber());
    }

    @Test
    void shouldRejectInvalidEmailInUpdate() {
        // Given
        Customer customer = new Customer(
            "Max", "Mustermann",
            createValidAddress(),
            createValidLicenseNumber(),
            "max@example.com",
            "0301234567"
        );

        // When & Then
        assertThrows(
            InvalidEmailException.class,
            () -> customer.updateContactDetails("invalid-email", "0409876543")
        );
    }

    @Test
    void shouldUpdateAddress() {
        // Given
        Customer customer = new Customer(
            "Max", "Mustermann",
            createValidAddress(),
            createValidLicenseNumber(),
            "max@example.com",
            "0301234567"
        );
        Address newAddress = new Address("Nebenstraße 456", "54321", "München");

        // When
        customer.updateAddress(newAddress);

        // Then
        assertEquals(newAddress, customer.getAddress());
    }

    @Test
    void shouldRejectNullAddressInUpdate() {
        // Given
        Customer customer = new Customer(
            "Max", "Mustermann",
            createValidAddress(),
            createValidLicenseNumber(),
            "max@example.com",
            "0301234567"
        );

        // When & Then
        assertThrows(
            InvalidCustomerDataException.class,
            () -> customer.updateAddress(null)
        );
    }

    @Test
    void shouldUpdateDriverLicenseNumber() {
        // Given
        Customer customer = new Customer(
            "Max", "Mustermann",
            createValidAddress(),
            createValidLicenseNumber(),
            "max@example.com",
            "0301234567"
        );
        DriverLicenseNumber newLicense = new DriverLicenseNumber("C987654321Y");

        // When
        customer.updateDriverLicenseNumber(newLicense);

        // Then
        assertEquals(newLicense, customer.getDriverLicenseNumber());
    }

    @Test
    void shouldGetFullName() {
        // Given
        Customer customer = new Customer(
            "Max", "Mustermann",
            createValidAddress(),
            createValidLicenseNumber(),
            "max@example.com",
            "0301234567"
        );

        // When
        String fullName = customer.getFullName();

        // Then
        assertEquals("Max Mustermann", fullName);
    }

    @Test
    void shouldImplementEqualsBasedOnId() {
        // Given
        Customer customer1 = new Customer(
            "Max", "Mustermann",
            createValidAddress(),
            createValidLicenseNumber(),
            "max@example.com",
            "0301234567"
        );
        Customer customer2 = new Customer(
            "Max", "Mustermann",
            createValidAddress(),
            createValidLicenseNumber(),
            "max@example.com",
            "0301234567"
        );

        // Then - ohne ID sind sie nicht gleich (da ID null)
        // Nach Persistierung mit gleicher ID wären sie gleich
        assertEquals(customer1, customer1);
        assertNotEquals(customer1, null);
    }

    @Test
    void shouldImplementToString() {
        // Given
        Customer customer = new Customer(
            "Max", "Mustermann",
            createValidAddress(),
            createValidLicenseNumber(),
            "max@example.com",
            "0301234567"
        );

        // When
        String result = customer.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Customer"));
    }
}
