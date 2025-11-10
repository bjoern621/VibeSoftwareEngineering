package com.travelreimburse.domain.service;

import com.travelreimburse.domain.model.*;
import com.travelreimburse.domain.repository.TravelPolicyRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain Service für die Validierung von Reiseanträgen gegen Reiserichtlinien
 * 
 * DDD: Domain Service - enthält Geschäftslogik die nicht zu einer Entity gehört
 * Validiert ob ein TravelRequest die Policy-Limits einhält
 */
@Service
public class TravelPolicyValidator {
    
    private final TravelPolicyRepository policyRepository;
    
    public TravelPolicyValidator(TravelPolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }
    
    /**
     * Validiert einen Reiseantrag gegen alle anwendbaren Policies
     * 
     * @param travelRequest der zu validierende Reiseantrag
     * @param departmentCode Abteilung des Mitarbeiters
     * @param location Standort des Mitarbeiters
     * @return ValidationResult mit Ergebnis und Violations
     */
    public ValidationResult validate(TravelRequest travelRequest, 
                                    String departmentCode, 
                                    String location) {
        List<String> violations = new ArrayList<>();
        
        // Finde anwendbare Policies
        List<TravelPolicy> applicablePolicies = findApplicablePolicies(departmentCode, location);
        
        if (applicablePolicies.isEmpty()) {
            // Keine Policy gefunden - konservativ: manuelle Genehmigung erforderlich
            return ValidationResult.requiresManualApproval("Keine anwendbare Reiserichtlinie gefunden");
        }
        
        // Prüfe gegen erste anwendbare Policy (priorität: spezifischste zuerst)
        TravelPolicy policy = applicablePolicies.get(0);
        
        // HINWEIS: Hier würden wir normalerweise gegen einzelne Expense-Kategorien prüfen
        // Da wir noch kein vollständiges Expense-System haben, prüfen wir nur ob Policy existiert
        
        // Wenn Policy Auto-Approval erlaubt und keine Violations, dann automatisch genehmigen
        if (violations.isEmpty() && policy.isAutoApprovalEnabled()) {
            return ValidationResult.autoApproved(policy.getName());
        }
        
        // Sonst: manuelle Genehmigung erforderlich
        if (violations.isEmpty()) {
            return ValidationResult.requiresManualApproval(
                "Policy '" + policy.getName() + "' erlaubt keine automatische Genehmigung"
            );
        }
        
        return ValidationResult.rejected(violations);
    }
    
    /**
     * Findet alle anwendbaren Policies für Department/Location
     * Sortiert nach Spezifität: Department+Location > Department > Location > Global
     */
    private List<TravelPolicy> findApplicablePolicies(String departmentCode, String location) {
        List<TravelPolicy> allPolicies = policyRepository.findAllActive();
        List<TravelPolicy> applicable = new ArrayList<>();
        
        for (TravelPolicy policy : allPolicies) {
            if (policy.appliesTo(departmentCode, location)) {
                applicable.add(policy);
            }
        }
        
        // Sortiere nach Spezifität (spezifischste zuerst)
        applicable.sort((p1, p2) -> {
            int score1 = getPolicySpecificityScore(p1);
            int score2 = getPolicySpecificityScore(p2);
            return Integer.compare(score2, score1); // Absteigend
        });
        
        return applicable;
    }
    
    /**
     * Berechnet Spezifitäts-Score einer Policy
     * Höherer Score = spezifischer
     */
    private int getPolicySpecificityScore(TravelPolicy policy) {
        int score = 0;
        if (policy.getDepartmentCode() != null) score += 2;
        if (policy.getLocation() != null) score += 1;
        return score;
    }
    
    /**
     * Prüft ob für eine Department/Location Auto-Approval aktiviert ist
     */
    public boolean isAutoApprovalEnabled(String departmentCode, String location) {
        List<TravelPolicy> applicablePolicies = findApplicablePolicies(departmentCode, location);
        
        if (applicablePolicies.isEmpty()) {
            return false;
        }
        
        return applicablePolicies.get(0).isAutoApprovalEnabled();
    }
    
    /**
     * Value Object für Validierungsergebnisse
     */
    public static class ValidationResult {
        private final boolean valid;
        private final boolean autoApproved;
        private final String message;
        private final List<String> violations;
        
        private ValidationResult(boolean valid, boolean autoApproved, 
                                String message, List<String> violations) {
            this.valid = valid;
            this.autoApproved = autoApproved;
            this.message = message;
            this.violations = violations != null ? violations : new ArrayList<>();
        }
        
        public static ValidationResult autoApproved(String policyName) {
            return new ValidationResult(true, true, 
                "Automatisch genehmigt durch Policy: " + policyName, null);
        }
        
        public static ValidationResult requiresManualApproval(String reason) {
            return new ValidationResult(true, false, reason, null);
        }
        
        public static ValidationResult rejected(List<String> violations) {
            return new ValidationResult(false, false, 
                "Policy-Verletzungen gefunden", violations);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public boolean isAutoApproved() {
            return autoApproved;
        }
        
        public String getMessage() {
            return message;
        }
        
        public List<String> getViolations() {
            return new ArrayList<>(violations);
        }
    }
}
