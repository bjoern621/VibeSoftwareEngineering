package com.travelreimburse.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;

/**
 * Value Object für Kostenstelle
 * 
 * DDD: Immutable Value Object - keine Identität, keine Setter
 * Repräsentiert eine Kostenstelle mit Code und Name
 */
@Embeddable
public class CostCenter {

    @Column(name = "cost_center_code", length = 20)
    private final String code;
    
    @Column(name = "cost_center_name", length = 100)
    private final String name;

    /**
     * JPA benötigt Default-Konstruktor (nur für Framework-Zugriff)
     * DDD: Final fields werden hier mit null initialisiert (JPA setzt echte Werte via Reflection)
     */
    protected CostCenter() {
        this.code = null;
        this.name = null;
    }

    /**
     * Konstruktor mit Validierung
     * DDD: Invarianten werden hier geprüft
     */
    public CostCenter(String code, String name) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Kostenstellen-Code darf nicht leer sein");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Kostenstellen-Name darf nicht leer sein");
        }
        if (code.length() > 20) {
            throw new IllegalArgumentException("Kostenstellen-Code darf maximal 20 Zeichen lang sein");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Kostenstellen-Name darf maximal 100 Zeichen lang sein");
        }
        
        this.code = code.trim().toUpperCase(); // Normalisierung
        this.name = name.trim();
    }

    /**
     * Business-Methode: Prüft ob es sich um eine spezielle Kostenstelle handelt
     * z.B. für besondere Genehmigungsregeln
     */
    public boolean isExecutiveLevel() {
        return code.startsWith("EXEC-") || code.startsWith("CEO-");
    }

    /**
     * Business-Methode: Prüft ob Kostenstelle einem bestimmten Bereich angehört
     */
    public boolean belongsToDepartment(String departmentPrefix) {
        if (departmentPrefix == null) return false;
        return code.startsWith(departmentPrefix.toUpperCase());
    }

    // Immutable - nur Getter, keine Setter!

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CostCenter that = (CostCenter) o;
        return Objects.equals(code, that.code) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name);
    }

    @Override
    public String toString() {
        return "CostCenter{" +
               "code='" + code + '\'' +
               ", name='" + name + '\'' +
               '}';
    }
}
