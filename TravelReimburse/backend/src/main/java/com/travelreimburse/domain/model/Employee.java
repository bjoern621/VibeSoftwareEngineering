package com.travelreimburse.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Employee Entity (Aggregate Root)
 * 
 * Repräsentiert einen Mitarbeiter im TRAVELREIMBURSE-System.
 * 
 * DDD: Rich Domain Model mit Business-Methoden
 * - hasRole(): Prüft Rolle
 * - isManager(): Prüft ob Führungskraft
 * - canApprove(): Prüft Genehmigungsberechtigung
 * - activate()/deactivate(): Aktivierungsstatus ändern
 * - changeRole(): Rolle ändern
 */
@Entity
@Table(name = "employees")
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String firstName;
    
    @Column(nullable = false, length = 100)
    private String lastName;
    
    @Column(nullable = false, unique = true, length = 255)
    private String email;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeRole role;
    
    @Column
    private Long managerId;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime deactivatedAt;
    
    // JPA benötigt Default-Konstruktor
    protected Employee() {
    }
    
    /**
     * Erstellt einen neuen aktiven Mitarbeiter
     * 
     * @param firstName Vorname
     * @param lastName Nachname
     * @param email E-Mail-Adresse (muss eindeutig sein)
     * @param role Rolle des Mitarbeiters
     */
    public Employee(String firstName, String lastName, String email, EmployeeRole role) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("Vorname darf nicht leer sein");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Nachname darf nicht leer sein");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("E-Mail darf nicht leer sein");
        }
        if (!email.contains("@")) {
            throw new IllegalArgumentException("E-Mail muss gültig sein");
        }
        if (role == null) {
            throw new IllegalArgumentException("Rolle darf nicht null sein");
        }
        
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
        this.email = email.trim().toLowerCase();
        this.role = role;
        this.active = true;
    }
    
    /**
     * JPA Lifecycle-Callback: Setzt createdAt vor dem ersten Speichern
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
    
    /**
     * Business-Methode: Prüft ob Mitarbeiter eine bestimmte Rolle hat
     */
    public boolean hasRole(EmployeeRole role) {
        return this.role == role;
    }
    
    /**
     * Business-Methode: Prüft ob Mitarbeiter eine Führungskraft ist
     */
    public boolean isManager() {
        return this.role == EmployeeRole.MANAGER;
    }
    
    /**
     * Business-Methode: Prüft ob Mitarbeiter Reiseanträge genehmigen kann
     */
    public boolean canApprove() {
        if (!active) {
            return false;
        }
        return role.canApprove();
    }
    
    /**
     * Business-Methode: Prüft ob Mitarbeiter Delegationen erstellen kann
     */
    public boolean canDelegate() {
        if (!active) {
            return false;
        }
        return role.canDelegate();
    }
    
    /**
     * Business-Methode: Prüft ob Mitarbeiter im Namen anderer handeln kann
     */
    public boolean canActOnBehalf() {
        if (!active) {
            return false;
        }
        return role.canActOnBehalf();
    }
    
    /**
     * Business-Methode: Prüft ob Mitarbeiter finale Freigaben erteilen kann
     */
    public boolean canReleaseFunds() {
        if (!active) {
            return false;
        }
        return role.canReleaseFunds();
    }
    
    /**
     * Business-Methode: Deaktiviert den Mitarbeiter
     * Deaktivierte Mitarbeiter können keine Aktionen ausführen
     */
    public void deactivate() {
        if (!this.active) {
            throw new IllegalStateException("Mitarbeiter ist bereits deaktiviert");
        }
        this.active = false;
        this.deactivatedAt = LocalDateTime.now();
    }
    
    /**
     * Business-Methode: Aktiviert einen deaktivierten Mitarbeiter wieder
     */
    public void activate() {
        if (this.active) {
            throw new IllegalStateException("Mitarbeiter ist bereits aktiv");
        }
        this.active = true;
        this.deactivatedAt = null;
    }
    
    /**
     * Business-Methode: Ändert die Rolle des Mitarbeiters
     */
    public void changeRole(EmployeeRole newRole) {
        if (newRole == null) {
            throw new IllegalArgumentException("Neue Rolle darf nicht null sein");
        }
        if (this.role == newRole) {
            throw new IllegalStateException("Mitarbeiter hat bereits diese Rolle");
        }
        this.role = newRole;
    }
    
    /**
     * Business-Methode: Setzt den Vorgesetzten
     */
    public void assignManager(Long managerId) {
        if (managerId != null && managerId.equals(this.id)) {
            throw new IllegalArgumentException("Mitarbeiter kann nicht sein eigener Vorgesetzter sein");
        }
        this.managerId = managerId;
    }
    
    /**
     * Business-Methode: Aktualisiert die E-Mail-Adresse
     */
    public void updateEmail(String newEmail) {
        if (newEmail == null || newEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("E-Mail darf nicht leer sein");
        }
        if (!newEmail.contains("@")) {
            throw new IllegalArgumentException("E-Mail muss gültig sein");
        }
        this.email = newEmail.trim().toLowerCase();
    }
    
    /**
     * Business-Methode: Gibt den vollständigen Namen zurück
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    // Getters (keine Setter - Business-Methoden verwenden!)
    
    public Long getId() {
        return id;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public EmployeeRole getRole() {
        return role;
    }
    
    public Long getManagerId() {
        return managerId;
    }
    
    public Boolean isActive() {
        return active;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getDeactivatedAt() {
        return deactivatedAt;
    }
}
