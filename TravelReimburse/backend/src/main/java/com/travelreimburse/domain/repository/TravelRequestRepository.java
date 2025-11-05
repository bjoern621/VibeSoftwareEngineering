package com.travelreimburse.domain.repository;

import com.travelreimburse.domain.model.TravelRequest;
import java.util.List;
import java.util.Optional;

/**
 * Repository-Interface für TravelRequest (Domain Layer)
 * Definiert die Persistierungs-Operationen ohne technische Details
 * Die konkrete Implementierung erfolgt im Infrastructure Layer
 */
public interface TravelRequestRepository {
    
    /**
     * Speichert einen Reiseantrag (erstellt neu oder aktualisiert bestehenden)
     * @param travelRequest der zu speichernde Reiseantrag
     * @return der gespeicherte Reiseantrag mit ID
     */
    TravelRequest save(TravelRequest travelRequest);
    
    /**
     * Findet einen Reiseantrag anhand seiner ID
     * @param id die ID des Reiseantrags
     * @return Optional mit dem Reiseantrag, oder leer wenn nicht gefunden
     */
    Optional<TravelRequest> findById(Long id);
    
    /**
     * Findet alle Reiseanträge eines bestimmten Mitarbeiters
     * @param employeeId die ID des Mitarbeiters
     * @return Liste aller Reiseanträge des Mitarbeiters
     */
    List<TravelRequest> findByEmployeeId(Long employeeId);
}
