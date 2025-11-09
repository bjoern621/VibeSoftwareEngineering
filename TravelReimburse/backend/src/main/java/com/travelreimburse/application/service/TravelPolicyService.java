package com.travelreimburse.application.service;

import com.travelreimburse.application.dto.CreateTravelPolicyDTO;
import com.travelreimburse.application.dto.TravelPolicyResponseDTO;
import com.travelreimburse.domain.model.*;
import com.travelreimburse.domain.repository.TravelPolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Application Service für TravelPolicies
 * Orchestriert Use Cases für Reiserichtlinien
 * 
 * DDD: Service ist Orchestrator - ruft Entity-Methoden auf
 */
@Service
@Transactional(readOnly = true)
public class TravelPolicyService {
    
    private final TravelPolicyRepository repository;
    
    public TravelPolicyService(TravelPolicyRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Erstellt eine neue TravelPolicy
     * 
     * @param dto Daten für die neue Policy
     * @return Erstellte Policy als DTO
     */
    @Transactional
    public TravelPolicyResponseDTO createPolicy(CreateTravelPolicyDTO dto) {
        // Entity erstellen
        TravelPolicy policy = new TravelPolicy(
            dto.name(),
            dto.description(),
            dto.departmentCode(),
            dto.location(),
            dto.autoApprovalEnabled()
        );
        
        // Category Limits setzen
        if (dto.categoryLimits() != null) {
            for (Map.Entry<String, String> entry : dto.categoryLimits().entrySet()) {
                ExpenseCategory category = ExpenseCategory.valueOf(entry.getKey());
                Money limit = new Money(new BigDecimal(entry.getValue()), Currency.EUR);
                policy.setCategoryLimit(category, limit);
            }
        }
        
        // Speichern
        TravelPolicy saved = repository.save(policy);
        return toResponseDTO(saved);
    }
    
    /**
     * Findet alle aktiven Policies
     * 
     * @return Liste aller aktiven Policies
     */
    public List<TravelPolicyResponseDTO> findAllActive() {
        return repository.findAllActive().stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Findet alle Policies (aktiv und inaktiv)
     * 
     * @return Liste aller Policies
     */
    public List<TravelPolicyResponseDTO> findAll() {
        return repository.findAll().stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Findet eine Policy anhand ihrer ID
     * 
     * @param id Policy-ID
     * @return Policy als DTO
     */
    public TravelPolicyResponseDTO findById(Long id) {
        TravelPolicy policy = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Policy mit ID " + id + " nicht gefunden"));
        return toResponseDTO(policy);
    }
    
    /**
     * Aktiviert eine Policy
     * 
     * @param id Policy-ID
     * @return Aktualisierte Policy
     */
    @Transactional
    public TravelPolicyResponseDTO activatePolicy(Long id) {
        TravelPolicy policy = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Policy mit ID " + id + " nicht gefunden"));
        
        policy.activate();
        TravelPolicy saved = repository.save(policy);
        return toResponseDTO(saved);
    }
    
    /**
     * Deaktiviert eine Policy
     * 
     * @param id Policy-ID
     * @return Aktualisierte Policy
     */
    @Transactional
    public TravelPolicyResponseDTO deactivatePolicy(Long id) {
        TravelPolicy policy = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Policy mit ID " + id + " nicht gefunden"));
        
        policy.deactivate();
        TravelPolicy saved = repository.save(policy);
        return toResponseDTO(saved);
    }
    
    /**
     * Mapper: Entity -> DTO
     */
    private TravelPolicyResponseDTO toResponseDTO(TravelPolicy policy) {
        // Konvertiere Map<ExpenseCategory, String> zu Map<String, String>
        Map<String, String> limitsMap = new HashMap<>();
        for (Map.Entry<ExpenseCategory, String> entry : policy.getCategoryLimits().entrySet()) {
            limitsMap.put(entry.getKey().name(), entry.getValue());
        }
        
        return new TravelPolicyResponseDTO(
            policy.getId(),
            policy.getName(),
            policy.getDescription(),
            policy.getDepartmentCode(),
            policy.getLocation(),
            limitsMap,
            policy.isAutoApprovalEnabled(),
            policy.isActive(),
            policy.getCreatedAt(),
            policy.getUpdatedAt()
        );
    }
}
