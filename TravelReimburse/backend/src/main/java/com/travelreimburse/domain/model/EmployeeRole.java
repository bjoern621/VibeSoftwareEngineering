package com.travelreimburse.domain.model;

/**
 * Rollen im TRAVELREIMBURSE-System
 * 
 * Jede Rolle hat spezifische Berechtigungen:
 * - EMPLOYEE: Reisen beantragen, Belege hochladen
 * - MANAGER: Anträge genehmigen/ablehnen
 * - HR: Auswertungen, Reiserichtlinien verwalten
 * - ASSISTANT: Im Namen anderer handeln (Delegation)
 * - FINANCE: Finale Freigabe zur Auszahlung
 */
public enum EmployeeRole {
    EMPLOYEE("Mitarbeiter"),
    MANAGER("Führungskraft"),
    HR("Personalabteilung"),
    ASSISTANT("Assistent"),
    FINANCE("Finanzabteilung");
    
    private final String displayName;
    
    EmployeeRole(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Prüft ob diese Rolle Reiseanträge genehmigen kann
     */
    public boolean canApprove() {
        return this == MANAGER || this == HR;
    }
    
    /**
     * Prüft ob diese Rolle Delegationen erstellen kann
     */
    public boolean canDelegate() {
        return this == EMPLOYEE || this == MANAGER;
    }
    
    /**
     * Prüft ob diese Rolle im Namen anderer handeln kann
     */
    public boolean canActOnBehalf() {
        return this == ASSISTANT;
    }
    
    /**
     * Prüft ob diese Rolle finale Freigaben erteilen kann
     */
    public boolean canReleaseFunds() {
        return this == FINANCE;
    }
}
