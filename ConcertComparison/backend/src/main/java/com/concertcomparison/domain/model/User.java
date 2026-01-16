package com.concertcomparison.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * User Entity (Aggregate Root).
 * 
 * Repräsentiert einen Benutzer im Concert Comparison System.
 * 
 * DDD Aggregate Root:
 * - Verwaltet Benutzerdaten und Authentifizierung
 * - Business Logic für User-Lifecycle (Registrierung, Login, Profil-Update)
 * - Rollen-basierte Zugriffskontrolle (RBAC)
 * 
 * Business Rules:
 * - Email muss eindeutig sein
 * - Email muss gültiges Format haben
 * - Password muss gehashed gespeichert werden (BCrypt)
 * - Rolle bestimmt Berechtigungen (USER, ADMIN)
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email", unique = true),
    @Index(name = "idx_user_role", columnList = "role")
})
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Email-Adresse (eindeutig, dient auch als Username).
     */
    @Email(message = "Email muss gültiges Format haben")
    @NotBlank(message = "Email darf nicht leer sein")
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;
    
    /**
     * Gehashtes Passwort (BCrypt).
     * WICHTIG: Niemals plain-text Passwort speichern!
     */
    @NotBlank(message = "Password darf nicht leer sein")
    @Column(name = "password", nullable = false, length = 255)
    private String password;
    
    /**
     * Vorname des Benutzers.
     */
    @NotBlank(message = "Vorname darf nicht leer sein")
    @Size(min = 1, max = 100, message = "Vorname muss zwischen 1 und 100 Zeichen lang sein")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;
    
    /**
     * Nachname des Benutzers.
     */
    @NotBlank(message = "Nachname darf nicht leer sein")
    @Size(min = 1, max = 100, message = "Nachname muss zwischen 1 und 100 Zeichen lang sein")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;
    
    /**
     * Benutzerrolle (USER oder ADMIN).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;
    
    /**
     * Account aktiv/inaktiv.
     * Deaktivierte Accounts können sich nicht einloggen.
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Optimistic Locking für Concurrency Control.
     */
    @Version
    @Column(name = "version")
    private Long version;
    
    // ==================== CONSTRUCTORS ====================
    
    /**
     * Default Constructor (JPA required).
     */
    protected User() {
        // Für JPA/Hibernate
    }
    
    /**
     * Private Constructor für Factory Method.
     */
    private User(String email, String hashedPassword, String firstName, String lastName, UserRole role) {
        this.email = email;
        this.password = hashedPassword;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.enabled = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // ==================== FACTORY METHODS ====================
    
    /**
     * Factory Method: Erstellt einen neuen User mit Validierung.
     * 
     * WICHTIG: Password muss bereits gehashed sein (z.B. mit BCryptPasswordEncoder).
     * 
     * @param email Email-Adresse (eindeutig, gültiges Format)
     * @param hashedPassword Gehashtes Passwort (BCrypt)
     * @param firstName Vorname (nicht leer)
     * @param lastName Nachname (nicht leer)
     * @param role Benutzerrolle
     * @return Neuer User im Status enabled=true
     * @throws IllegalArgumentException bei ungültigen Parametern
     */
    public static User createUser(String email, String hashedPassword, String firstName, 
                                   String lastName, UserRole role) {
        validateEmail(email);
        validatePassword(hashedPassword);
        validateName(firstName, "Vorname");
        validateName(lastName, "Nachname");
        Objects.requireNonNull(role, "UserRole darf nicht null sein");
        
        return new User(email, hashedPassword, firstName, lastName, role);
    }
    
    /**
     * Factory Method: Erstellt einen neuen User mit Rolle USER (Standard).
     * 
     * @param email Email-Adresse
     * @param hashedPassword Gehashtes Passwort
     * @param firstName Vorname
     * @param lastName Nachname
     * @return Neuer User mit Rolle USER
     */
    public static User createUser(String email, String hashedPassword, String firstName, String lastName) {
        return createUser(email, hashedPassword, firstName, lastName, UserRole.USER);
    }
    
    /**
     * Factory Method: Erstellt einen neuen Admin.
     * 
     * @param email Email-Adresse
     * @param hashedPassword Gehashtes Passwort
     * @param firstName Vorname
     * @param lastName Nachname
     * @return Neuer User mit Rolle ADMIN
     */
    public static User createAdmin(String email, String hashedPassword, String firstName, String lastName) {
        return createUser(email, hashedPassword, firstName, lastName, UserRole.ADMIN);
    }
    
    // ==================== BUSINESS METHODS ====================
    
    /**
     * Aktualisiert das Benutzerprofil.
     * 
     * @param firstName Neuer Vorname
     * @param lastName Neuer Nachname
     * @throws IllegalArgumentException bei ungültigen Parametern
     */
    public void updateProfile(String firstName, String lastName) {
        validateName(firstName, "Vorname");
        validateName(lastName, "Nachname");
        
        this.firstName = firstName;
        this.lastName = lastName;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Ändert das Passwort.
     * 
     * WICHTIG: newHashedPassword muss bereits gehashed sein!
     * 
     * @param newHashedPassword Neues gehashtes Passwort
     * @throws IllegalArgumentException wenn Passwort ungültig
     */
    public void changePassword(String newHashedPassword) {
        validatePassword(newHashedPassword);
        
        this.password = newHashedPassword;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Ändert die Email-Adresse.
     * 
     * @param newEmail Neue Email-Adresse
     * @throws IllegalArgumentException wenn Email ungültig
     */
    public void changeEmail(String newEmail) {
        validateEmail(newEmail);
        
        this.email = newEmail;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Deaktiviert den Account.
     * Deaktivierte Accounts können sich nicht einloggen.
     */
    public void disable() {
        this.enabled = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Aktiviert den Account.
     */
    public void enable() {
        this.enabled = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Prüft ob der User Admin ist.
     */
    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }
    
    /**
     * Prüft ob der Account aktiv ist.
     */
    public boolean isEnabled() {
        return this.enabled;
    }
    
    /**
     * Liefert den vollständigen Namen.
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    // ==================== VALIDATION ====================
    
    /**
     * Validiert eine Email-Adresse.
     * 
     * @param email Zu validierende Email
     * @throws IllegalArgumentException wenn Email ungültig
     */
    private static void validateEmail(String email) {
        Objects.requireNonNull(email, "Email darf nicht null sein");
        
        if (email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email darf nicht leer sein");
        }
        
        // Basis Email-Validierung (weitere Validierung über @Email Annotation)
        if (!email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("Email muss gültiges Format haben");
        }
    }
    
    /**
     * Validiert ein Passwort (muss bereits gehashed sein).
     * 
     * @param password Zu validierendes Passwort
     * @throws IllegalArgumentException wenn Passwort ungültig
     */
    private static void validatePassword(String password) {
        Objects.requireNonNull(password, "Password darf nicht null sein");
        
        if (password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password darf nicht leer sein");
        }
        
        // BCrypt Hashes haben typischerweise 60 Zeichen
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password (hashed) muss mindestens 8 Zeichen haben");
        }
    }
    
    /**
     * Validiert einen Namen (Vor- oder Nachname).
     * 
     * @param name Zu validierender Name
     * @param fieldName Name des Feldes für Fehlermeldung
     * @throws IllegalArgumentException wenn Name ungültig
     */
    private static void validateName(String name, String fieldName) {
        Objects.requireNonNull(name, fieldName + " darf nicht null sein");
        
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " darf nicht leer sein");
        }
        
        if (name.length() > 100) {
            throw new IllegalArgumentException(fieldName + " darf maximal 100 Zeichen haben");
        }
    }
    
    // ==================== JPA LIFECYCLE CALLBACKS ====================
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (enabled == null) {
            enabled = true;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // ==================== GETTERS ====================
    
    public Long getId() {
        return id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public Long getVersion() {
        return version;
    }
    
    // ==================== EQUALS & HASHCODE ====================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", role=" + role +
                ", enabled=" + enabled +
                '}';
    }
}
