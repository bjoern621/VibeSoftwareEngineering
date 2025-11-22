package com.rentacar.domain.model;

import com.rentacar.domain.exception.InvalidEmailException;
import com.rentacar.infrastructure.persistence.converter.EncryptedStringConverter;
import jakarta.persistence.*;
import java.time.LocalDateTime;
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
    // DEVELOPMENT: E-Mail-Verschlüsselung temporär deaktiviert für einfaches Login-Testen
    // TODO: Für Production wieder aktivieren: @Convert(converter = EncryptedStringConverter.class)
    // @Convert(converter = EncryptedStringConverter.class)
    private String email;

    @Column(length = 100)
    @Convert(converter = EncryptedStringConverter.class)
    private String phoneNumber;

    @Column(nullable = false)
    private String password; // BCrypt Hash - nicht verschlüsselt, da bereits gehasht

    @Column(nullable = false)
    private boolean emailVerified = false;

    @Column(length = 36)
    private String verificationToken;

    @Column
    private LocalDateTime tokenExpiryDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime lastModifiedAt;

    // JPA benötigt einen Default-Konstruktor
    protected Customer() {
    }

    /**
     * Erstellt einen neuen Kunden (für Registrierung).
     *
     * @param firstName           Vorname
     * @param lastName            Nachname
     * @param address             Adresse (Value Object)
     * @param driverLicenseNumber Führerscheinnummer (Value Object)
     * @param email               E-Mail-Adresse (eindeutig)
     * @param phoneNumber         Telefonnummer (optional)
     * @param password            BCrypt-gehashtes Passwort
     * @throws InvalidEmailException        wenn E-Mail-Format ungültig ist
     * @throws IllegalArgumentException     wenn andere Parameter ungültig sind
     */
    public Customer(String firstName,
                    String lastName,
                    Address address,
                    DriverLicenseNumber driverLicenseNumber,
                    String email,
                    String phoneNumber,
                    String password) {
        validateName(firstName, "Vorname");
        validateName(lastName, "Nachname");
        validateEmail(email);
        validatePassword(password);

        this.firstName = firstName;
        this.lastName = lastName;
        this.address = Objects.requireNonNull(address, "Adresse darf nicht null sein");
        this.driverLicenseNumber = Objects.requireNonNull(
            driverLicenseNumber,
            "Führerscheinnummer darf nicht null sein"
        );
        this.email = email.toLowerCase();
        this.phoneNumber = normalizePhoneNumber(phoneNumber);
        this.password = password;
        this.emailVerified = false;
        this.createdAt = LocalDateTime.now();
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * Erstellt einen neuen Kunden (Legacy-Konstruktor für bestehende Tests).
     * Deprecated: Verwende den Konstruktor mit Passwort-Parameter für neue Registrierungen.
     */
    @Deprecated
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
        this.password = null; // Für Legacy-Support
        this.emailVerified = false;
        this.createdAt = LocalDateTime.now();
        this.lastModifiedAt = LocalDateTime.now();
    }

    private void validateName(String name, String fieldName) {
        if (name == null || name.isBlank()) {
            throw new com.rentacar.domain.exception.InvalidCustomerDataException(
                fieldName, fieldName + " darf nicht leer sein");
        }
        if (name.length() < 2 || name.length() > 100) {
            throw new com.rentacar.domain.exception.InvalidCustomerDataException(
                fieldName, fieldName + " muss zwischen 2 und 100 Zeichen lang sein");
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

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new com.rentacar.domain.exception.InvalidCustomerDataException(
                "password", "Passwort darf nicht leer sein");
        }
        // Passwort sollte bereits BCrypt-gehasht sein
        // BCrypt-Hashes haben eine feste Länge von 60 Zeichen und beginnen mit "$2a$", "$2b$" oder "$2y$"
        if (!password.startsWith("$2") || password.length() != 60) {
            throw new com.rentacar.domain.exception.InvalidCustomerDataException(
                "password", "Passwort muss BCrypt-gehasht sein");
        }
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return null;
        }
        // Entfernt Leerzeichen und Bindestriche
        String normalized = phoneNumber.replaceAll("[\\s-]", "");
        if (normalized.length() < 5 || normalized.length() > 20) {
            throw new com.rentacar.domain.exception.InvalidCustomerDataException(
                "phoneNumber", "Telefonnummer muss zwischen 5 und 20 Zeichen lang sein");
        }
        return normalized;
    }

    // Business-Methoden (keine public Setters!)

    /**
     * Generiert einen Verifikations-Token für E-Mail-Bestätigung.
     * Token ist 24 Stunden gültig.
     *
     * @return der generierte Token
     */
    public String generateVerificationToken() {
        this.verificationToken = java.util.UUID.randomUUID().toString();
        this.tokenExpiryDate = LocalDateTime.now().plusHours(24);
        this.lastModifiedAt = LocalDateTime.now();
        return this.verificationToken;
    }

    /**
     * Verifiziert die E-Mail-Adresse mit dem gegebenen Token.
     *
     * @param token der Verifikations-Token
     * @throws IllegalArgumentException wenn Token ungültig oder abgelaufen ist
     */
    public void verifyEmail(String token) {
        if (this.emailVerified) {
            throw new com.rentacar.domain.exception.EmailAlreadyVerifiedException(
                "E-Mail wurde bereits verifiziert");
        }
        if (this.verificationToken == null || !this.verificationToken.equals(token)) {
            throw new com.rentacar.domain.exception.InvalidVerificationTokenException(
                "Ungültiger Verifikations-Token");
        }
        if (this.tokenExpiryDate == null || LocalDateTime.now().isAfter(this.tokenExpiryDate)) {
            throw new com.rentacar.domain.exception.ExpiredVerificationTokenException(
                "Verifikations-Token ist abgelaufen");
        }
        this.emailVerified = true;
        this.verificationToken = null;
        this.tokenExpiryDate = null;
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * Ändert das Passwort des Kunden.
     *
     * @param newPassword das neue BCrypt-gehashte Passwort
     */
    public void changePassword(String newPassword) {
        validatePassword(newPassword);
        this.password = newPassword;
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * Aktualisiert die Kontaktdaten des Kunden.
     *
     * @param newEmail       neue E-Mail-Adresse
     * @param newPhoneNumber neue Telefonnummer
     */
    public void updateContactDetails(String newEmail, String newPhoneNumber) {
        validateEmail(newEmail);
        // Wenn E-Mail geändert wird, muss diese erneut verifiziert werden
        if (!this.email.equals(newEmail.toLowerCase())) {
            this.emailVerified = false;
        }
        this.email = newEmail.toLowerCase();
        this.phoneNumber = normalizePhoneNumber(newPhoneNumber);
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * Aktualisiert die Adresse des Kunden.
     *
     * @param newAddress neue Adresse
     */
    public void updateAddress(Address newAddress) {
        this.address = Objects.requireNonNull(newAddress, "Adresse darf nicht null sein");
        this.lastModifiedAt = LocalDateTime.now();
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
        this.lastModifiedAt = LocalDateTime.now();
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

    public String getPassword() {
        return password;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public LocalDateTime getTokenExpiryDate() {
        return tokenExpiryDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastModifiedAt() {
        return lastModifiedAt;
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
