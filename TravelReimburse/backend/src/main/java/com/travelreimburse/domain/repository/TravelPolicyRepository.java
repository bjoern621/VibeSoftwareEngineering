package com.travelreimburse.domain.repository;

import com.travelreimburse.domain.model.TravelPolicy;
import java.util.List;
import java.util.Optional;

/**
 * Repository-Interface für TravelPolicy (Domain Layer)
 * Definiert die Persistierungs-Operationen ohne technische Details
 * Die konkrete Implementierung erfolgt im Infrastructure Layer
 * 
 * DDD: Repository-Interface im Domain Layer (abstrakt)
 */
public interface TravelPolicyRepository {
    
    /**
     * Speichert eine TravelPolicy
     * 
     * @param policy zu speichernde Policy
     * @return gespeicherte Policy mit ID
     */
    TravelPolicy save(TravelPolicy policy);
    
    /**
     * Findet eine Policy anhand ihrer ID
     * 
     * @param id die ID der Policy
     * @return Optional mit Policy oder leer
     */
    Optional<TravelPolicy> findById(Long id);
    
    /**
     * Findet alle aktiven Policies
     * 
     * @return Liste aller aktiven Policies
     */
    List<TravelPolicy> findAllActive();
    
    /**
     * Findet alle Policies (aktive und inaktive)
     * 
     * @return Liste aller Policies
     */
    List<TravelPolicy> findAll();
    
    /**
     * Findet Policies die für eine Abteilung gelten
     * 
     * @param departmentCode Abteilungs-Code
     * @return Liste der anwendbaren Policies
     */
    List<TravelPolicy> findByDepartmentCode(String departmentCode);
    
    /**
     * Findet Policies die für einen Standort gelten
     * 
     * @param location Standort
     * @return Liste der anwendbaren Policies
     */
    List<TravelPolicy> findByLocation(String location);
    
    /**
     * Findet aktive Policies mit Auto-Approval
     * 
     * @return Liste der Policies mit aktivierter Auto-Genehmigung
     */
    List<TravelPolicy> findActiveWithAutoApproval();
    
    /**
     * Löscht eine Policy anhand ihrer ID
     * 
     * @param id die ID der zu löschenden Policy
     */
    void deleteById(Long id);
}
