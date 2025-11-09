package com.travelreimburse.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity für Reiserichtlinien (Travel Policies)
 * 
 * Definiert Höchstbeträge und Pauschalen für verschiedene Ausgabenkategorien.
 * Kann nach Abteilung, Standort oder allgemein gelten.
 * 
 * DDD: Aggregate Root mit Business-Methoden
 * - Validiert Limits
 * - Prüft Anwendbarkeit für Department/Location
 */
@Entity
@Table(name = "travel_policies")
public class TravelPolicy {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    /**
     * Optional: Abteilungs-Code für den diese Policy gilt
     * NULL = gilt für alle Abteilungen
     */
    @Column(length = 50)
    private String departmentCode;
    
    /**
     * Optional: Standort für den diese Policy gilt
     * NULL = gilt für alle Standorte
     */
    @Column(length = 100)
    private String location;
    
    /**
     * Höchstbeträge pro Ausgabenkategorie (in EUR)
     * Key: ExpenseCategory
     * Value: Höchstbetrag als BigDecimal (wird als String gespeichert)
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "policy_category_limits", 
                     joinColumns = @JoinColumn(name = "policy_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "expense_category")
    @Column(name = "max_amount")
    private Map<ExpenseCategory, String> categoryLimits = new HashMap<>();
    
    /**
     * Ob diese Policy automatische Genehmigung erlaubt
     * true = Anträge innerhalb der Limits werden automatisch genehmigt
     * false = Anträge müssen manuell genehmigt werden
     */
    @Column(nullable = false)
    private boolean autoApprovalEnabled = false;
    
    @Column(nullable = false)
    private boolean active = true;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    // JPA benötigt Default-Konstruktor
    protected TravelPolicy() {
    }
    
    /**
     * Erstellt eine neue Reiserichtlinie
     * 
     * @param name Name der Policy
     * @param description Beschreibung
     * @param departmentCode Optional: Abteilungs-Code (null = alle)
     * @param location Optional: Standort (null = alle)
     * @param autoApprovalEnabled Automatische Genehmigung aktivieren
     */
    public TravelPolicy(String name, String description, 
                       String departmentCode, String location,
                       boolean autoApprovalEnabled) {
        validateParameters(name);
        
        this.name = name;
        this.description = description;
        this.departmentCode = departmentCode;
        this.location = location;
        this.autoApprovalEnabled = autoApprovalEnabled;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }
    
    private void validateParameters(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Policy-Name darf nicht leer sein");
        }
    }
    
    /**
     * Business-Methode: Setzt ein Limit für eine Ausgabenkategorie
     * 
     * @param category Ausgabenkategorie
     * @param maxAmount Höchstbetrag in EUR
     */
    public void setCategoryLimit(ExpenseCategory category, Money maxAmount) {
        if (category == null) {
            throw new IllegalArgumentException("Kategorie darf nicht null sein");
        }
        if (maxAmount == null) {
            throw new IllegalArgumentException("Betrag darf nicht null sein");
        }
        if (!maxAmount.getCurrency().equals(Currency.EUR)) {
            throw new IllegalArgumentException("Policy-Limits müssen in EUR angegeben werden");
        }
        
        this.categoryLimits.put(category, maxAmount.getAmount().toString());
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Business-Methode: Gibt das Limit für eine Kategorie zurück
     * 
     * @param category Ausgabenkategorie
     * @return Money-Objekt mit Limit oder null wenn kein Limit gesetzt
     */
    public Money getCategoryLimit(ExpenseCategory category) {
        String limitStr = categoryLimits.get(category);
        if (limitStr == null) {
            return null;
        }
        return new Money(new java.math.BigDecimal(limitStr), Currency.EUR);
    }
    
    /**
     * Business-Methode: Prüft ob diese Policy für eine Abteilung/Standort gilt
     * 
     * @param departmentCode Abteilungs-Code des Mitarbeiters
     * @param location Standort des Mitarbeiters
     * @return true wenn Policy anwendbar ist
     */
    public boolean appliesTo(String departmentCode, String location) {
        // Wenn Policy-Department null ist, gilt sie für alle Departments
        boolean departmentMatches = (this.departmentCode == null || 
                                    this.departmentCode.equals(departmentCode));
        
        // Wenn Policy-Location null ist, gilt sie für alle Standorte
        boolean locationMatches = (this.location == null || 
                                  this.location.equals(location));
        
        return departmentMatches && locationMatches && active;
    }
    
    /**
     * Business-Methode: Aktiviert die Policy
     */
    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Business-Methode: Deaktiviert die Policy
     */
    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Business-Methode: Aktualisiert Name und Beschreibung
     */
    public void updateDetails(String name, String description) {
        validateParameters(name);
        this.name = name;
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Business-Methode: Aktiviert/Deaktiviert Auto-Approval
     */
    public void setAutoApproval(boolean enabled) {
        this.autoApprovalEnabled = enabled;
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getDepartmentCode() {
        return departmentCode;
    }
    
    public String getLocation() {
        return location;
    }
    
    public Map<ExpenseCategory, String> getCategoryLimits() {
        return new HashMap<>(categoryLimits); // Defensive copy
    }
    
    public boolean isAutoApprovalEnabled() {
        return autoApprovalEnabled;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
