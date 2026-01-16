package com.concertcomparison.domain.model;

import com.concertcomparison.domain.exception.InvalidConcertDateException;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Concert Aggregate Root.
 * 
 * Repräsentiert ein Konzert/Event mit allen Metadaten.
 * 
 * DDD Aggregate Root:
 * - Verwaltet Konzert-Metadaten (Name, Datum, Venue, Beschreibung)
 * - Business Logic für Konzert-Lifecycle (Erstellen, Aktualisieren)
 * - Validierung von Business Invariants im Entity
 * 
 * Beziehung zu Seats:
 * - Concert und Seat sind separate Aggregates (kein bidirektionales Mapping)
 * - Seats referenzieren Concert via concertId
 * - Ermöglicht bessere Performance und Skalierbarkeit
 * 
 * Business Rules:
 * - Konzertdatum muss in der Zukunft liegen
 * - Name, Venue dürfen nicht leer sein
 * - Beschreibung ist optional
 */
@Entity
@Table(name = "concerts", indexes = {
    @Index(name = "idx_concert_date", columnList = "event_date"),
    @Index(name = "idx_concert_venue", columnList = "venue")
})
public class Concert {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    @Column(name = "event_date", nullable = false)
    private LocalDateTime date;
    
    @Column(name = "venue", nullable = false, length = 500)
    private String venue;
    
    @Column(name = "description", length = 2000)
    private String description;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Optimistic Locking für Concurrency Control.
     * Verhindert Lost Updates bei parallelen Aktualisierungen.
     */
    @Version
    @Column(name = "version")
    private Long version;
    
    // ==================== CONSTRUCTORS ====================
    
    /**
     * Default Constructor (JPA required).
     */
    protected Concert() {
        // Für JPA/Hibernate
    }
    
    /**
     * Private Constructor für Factory Method.
     */
    private Concert(String name, LocalDateTime date, String venue, String description) {
        this.name = name;
        this.date = date;
        this.venue = venue;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // ==================== FACTORY METHODS ====================
    
    /**
     * Factory Method: Erstellt ein neues Concert mit Validierung.
     * 
     * @param name Name des Konzerts (nicht null, nicht leer)
     * @param date Datum des Konzerts (nicht null, muss in der Zukunft liegen)
     * @param venue Veranstaltungsort (nicht null, nicht leer)
     * @param description Beschreibung (optional, kann null sein)
     * @return Neues Concert-Objekt
     * @throws IllegalArgumentException wenn Name oder Venue null/leer sind
     * @throws InvalidConcertDateException wenn Datum null oder in der Vergangenheit liegt
     */
    public static Concert createConcert(String name, LocalDateTime date, String venue, String description) {
        validateName(name);
        validateDate(date);
        validateVenue(venue);
        
        return new Concert(name, date, venue, description);
    }
    
    // ==================== BUSINESS METHODS ====================
    
    /**
     * Aktualisiert die Konzert-Daten.
     * 
     * @param name Neuer Name (nicht null, nicht leer)
     * @param date Neues Datum (nicht null, muss in der Zukunft liegen)
     * @param venue Neuer Veranstaltungsort (nicht null, nicht leer)
     * @param description Neue Beschreibung (optional)
     * @throws IllegalArgumentException wenn Name oder Venue null/leer sind
     * @throws InvalidConcertDateException wenn Datum null oder in der Vergangenheit liegt
     */
    public void update(String name, LocalDateTime date, String venue, String description) {
        validateName(name);
        validateDate(date);
        validateVenue(venue);
        
        this.name = name;
        this.date = date;
        this.venue = venue;
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Prüft ob das Konzert bereits stattgefunden hat.
     * 
     * @return true wenn das Konzertdatum in der Vergangenheit liegt
     */
    public boolean isPastEvent() {
        return this.date.isBefore(LocalDateTime.now());
    }
    
    /**
     * Prüft ob das Konzert in der Zukunft liegt.
     * 
     * @return true wenn das Konzertdatum in der Zukunft liegt
     */
    public boolean isFutureEvent() {
        return this.date.isAfter(LocalDateTime.now());
    }
    
    // ==================== VALIDATION ====================
    
    /**
     * Validiert den Konzert-Namen.
     * 
     * @param name Zu validierender Name
     * @throws IllegalArgumentException wenn Name null oder leer ist
     */
    private static void validateName(String name) {
        Objects.requireNonNull(name, "Konzert-Name darf nicht null sein");
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Konzert-Name darf nicht leer sein");
        }
    }
    
    /**
     * Validiert das Konzert-Datum.
     * 
     * Business Rule: Konzerte können nur für zukünftige Daten angelegt werden.
     * 
     * @param date Zu validierendes Datum
     * @throws InvalidConcertDateException wenn Datum null oder in der Vergangenheit liegt
     */
    private static void validateDate(LocalDateTime date) {
        Objects.requireNonNull(date, "Konzert-Datum darf nicht null sein");
        if (date.isBefore(LocalDateTime.now())) {
            throw new InvalidConcertDateException(
                "Konzert-Datum muss in der Zukunft liegen. Angegebenes Datum: " + date
            );
        }
    }
    
    /**
     * Validiert den Veranstaltungsort.
     * 
     * @param venue Zu validierender Veranstaltungsort
     * @throws IllegalArgumentException wenn Venue null oder leer ist
     */
    private static void validateVenue(String venue) {
        Objects.requireNonNull(venue, "Veranstaltungsort darf nicht null sein");
        if (venue.trim().isEmpty()) {
            throw new IllegalArgumentException("Veranstaltungsort darf nicht leer sein");
        }
    }
    
    // ==================== JPA LIFECYCLE CALLBACKS ====================
    
    /**
     * JPA Callback: Wird vor dem ersten Persist aufgerufen.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * JPA Callback: Wird vor jedem Update aufgerufen.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // ==================== GETTERS ====================
    
    public Long getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public LocalDateTime getDate() {
        return date;
    }
    
    public String getVenue() {
        return venue;
    }
    
    public String getDescription() {
        return description;
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
    
    /**
     * Setzt die ID (nur für Tests).
     * 
     * @param id Die zu setzende ID
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    // ==================== EQUALS & HASHCODE ====================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Concert concert = (Concert) o;
        return Objects.equals(id, concert.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Concert{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", date=" + date +
                ", venue='" + venue + '\'' +
                ", version=" + version +
                '}';
    }
}
