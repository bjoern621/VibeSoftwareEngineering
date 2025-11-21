package com.rentacar.domain.model;

import com.rentacar.domain.exception.InvalidEmailException;
import com.rentacar.infrastructure.persistence.converter.EncryptedStringConverter;
import jakarta.persistence.*;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Customer Aggregate Root.
 * Verwaltet Kundendaten mit DSGVO-konformer Verschlüsselung sensibler Informationen.
 */
@Entity
@Table(name = "customers")
public class Customer {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    @Convert(converter = EncryptedStringConverter.class)
    private String firstName;

    @Column(nullable = false, length = 100)
    @Convert(converter = EncryptedStringConverter.class)
    private String lastName;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "street", column = @Column(name = "address_street", nullable = false, length = 500)),
        @AttributeOverride(name = "postalCode", column = @Column(name = "address_postal_code", nullable = false, length = 100)),
        @AttributeOverride(name = "city", column = @Column(name = "address_city", nullable = false, length = 500))
    })
    private Address address;

    @Embedded
    @AttributeOverride(name = "number", column = @Column(name = "driver_license_number", nullable = false, unique = true, length = 500))
    private DriverLicenseNumber driverLicenseNumber;

    @Column(nullable = false, unique = true, length = 500)
    @Convert(converter = EncryptedStringConverter.class)
    private String email;

    @Column(length = 100)
    @Convert(converter = EncryptedStringConverter.class)
    private String phoneNumber;

    // JPA benötigt einen Default-Konstruktor
    protected Customer() {
    }

    /**
     * Erstellt einen neuen Kunden.
     *
     * @param firstName           Vorname
     * @param lastName            Nachname
     * @param address             Adresse (Value Object)
     * @param driverLicenseNumber Führerscheinnummer (Value Object)
     * @param email               E-Mail-Adresse (eindeutig)
     * @param phoneNumber         Telefonnummer (optional)
     * @throws InvalidEmailException        wenn E-Mail-Format ungültig ist
     * @throws IllegalArgumentException     wenn andere Parameter ungültig sind
     */
    public Customer(String firstName,
                    String lastName,
                    Address address,
                    DriverLicenseNumber driverLicenseNumber,
                    String email,
                    String phoneNumber) {
        validateName(firstName, "Vorname");
        validateName(lastName, "Nachname");
        validateEmail(email);

        this.firstName = firstName;
        this.lastName = lastName;
        this.address = Objects.requireNonNull(address, "Adresse darf nicht null sein");
        this.driverLicenseNumber = Objects.requireNonNull(
            driverLicenseNumber,
            "Führerscheinnummer darf nicht null sein"
        );
        this.email = email.toLowerCase();
        this.phoneNumber = normalizePhoneNumber(phoneNumber);
    }

    private void validateName(String name, String fieldName) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(fieldName + " darf nicht leer sein");
        }
        if (name.length() < 2 || name.length() > 100) {
            throw new IllegalArgumentException(
                fieldName + " muss zwischen 2 und 100 Zeichen lang sein"
            );
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new InvalidEmailException("E-Mail darf nicht leer sein");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidEmailException(email);
        }
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return null;
        }
        // Entfernt Leerzeichen und Bindestriche
        String normalized = phoneNumber.replaceAll("[\\s-]", "");
        if (normalized.length() < 5 || normalized.length() > 20) {
            throw new IllegalArgumentException(
                "Telefonnummer muss zwischen 5 und 20 Zeichen lang sein"
            );
        }
        return normalized;
    }

    // Business-Methoden (keine public Setters!)

    /**
     * Aktualisiert die Kontaktdaten des Kunden.
     *
     * @param newEmail       neue E-Mail-Adresse
     * @param newPhoneNumber neue Telefonnummer
     */
    public void updateContactDetails(String newEmail, String newPhoneNumber) {
        validateEmail(newEmail);
        this.email = newEmail.toLowerCase();
        this.phoneNumber = normalizePhoneNumber(newPhoneNumber);
    }

    /**
     * Aktualisiert die Adresse des Kunden.
     *
     * @param newAddress neue Adresse
     */
    public void updateAddress(Address newAddress) {
        this.address = Objects.requireNonNull(newAddress, "Adresse darf nicht null sein");
    }

    /**
     * Aktualisiert die Führerscheinnummer.
     * ACHTUNG: Sollte nur in Ausnahmefällen verwendet werden (z.B. Fehlerkorrektur).
     *
     * @param newDriverLicenseNumber neue Führerscheinnummer
     */
    public void updateDriverLicenseNumber(DriverLicenseNumber newDriverLicenseNumber) {
        this.driverLicenseNumber = Objects.requireNonNull(
            newDriverLicenseNumber,
            "Führerscheinnummer darf nicht null sein"
        );
    }

    /**
     * Gibt den vollständigen Namen zurück.
     *
     * @return Vor- und Nachname
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    // Getter (keine Setter!)

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Address getAddress() {
        return address;
    }

    public DriverLicenseNumber getDriverLicenseNumber() {
        return driverLicenseNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(id, customer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Customer{" +
               "id=" + id +
               ", name='" + getFullName() + '\'' +
               ", email='" + email + '\'' +
               '}';
    }
}
