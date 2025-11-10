package com.travelreimburse.domain.repository;

import com.travelreimburse.domain.model.TravelRequest;
import com.travelreimburse.domain.model.TravelRequestStatus;
import java.time.LocalDate;
import java.time.LocalDate;
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

    /**
     * Findet alle eingereichten Reiseanträge die auf Genehmigung warten
     * @return Liste aller eingereichten Reiseanträge
     */
    List<TravelRequest> findPendingApprovals();

    // ========== REPORTING QUERIES (für HR-Auswertungen) ==========

    /**
     * Findet alle Reiseanträge in einem bestimmten Zeitraum (nach Reisedatum)
     * @param startDate Startdatum (inklusive)
     * @param endDate Enddatum (inklusive)
     * @return Liste aller Reiseanträge im Zeitraum
     */
    List<TravelRequest> findByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Findet alle Reiseanträge nach Erstellungsdatum (für HR-Reporting)
     * @param startDate Startdatum (inklusive)
     * @param endDate Enddatum (inklusive)
     * @return Liste aller Reiseanträge, die im Zeitraum erstellt wurden
     */
    List<TravelRequest> findByCreatedAtRange(LocalDate startDate, LocalDate endDate);

    /**
     * Findet alle Reiseanträge mit bestimmtem Status in einem Zeitraum
     * @param status gewünschter Status
     * @param startDate Startdatum (inklusive)
     * @param endDate Enddatum (inklusive)
     * @return Liste aller passenden Reiseanträge
     */
    List<TravelRequest> findByStatusAndDateRange(TravelRequestStatus status, 
                                                  LocalDate startDate, 
                                                  LocalDate endDate);

    /**
     * Zählt Reiseanträge nach Status in einem Zeitraum
     * @param status gewünschter Status
     * @param startDate Startdatum (inklusive)
     * @param endDate Enddatum (inklusive)
     * @return Anzahl der Reiseanträge
     */
    long countByStatusAndDateRange(TravelRequestStatus status, 
                                   LocalDate startDate, 
                                   LocalDate endDate);

    /**
     * Findet alle Reiseanträge für Reporting (alle Status)
     * @return Liste aller Reiseanträge
     */
    List<TravelRequest> findAll();

    /**
     * Findet Reiseanträge nach Mitarbeiter-IDs (für Kostenstellen-Reporting)
     * @param employeeIds Liste der Mitarbeiter-IDs
     * @return Liste der Reiseanträge
     */
    List<TravelRequest> findByEmployeeIdIn(List<Long> employeeIds);

    /**
     * Findet Reiseanträge nach Kostenstellen-Code
     * @param costCenterCode Code der Kostenstelle
     * @return Liste der Reiseanträge
     */
    List<TravelRequest> findByCostCenterCode(String costCenterCode);

    /**
     * Findet Reiseanträge nach Kostenstellen-Code in einem Zeitraum
     * @param costCenterCode Code der Kostenstelle
     * @param startDate Startdatum (inklusive)
     * @param endDate Enddatum (inklusive)
     * @return Liste der Reiseanträge
     */
    List<TravelRequest> findByCostCenterCodeAndDateRange(String costCenterCode,
                                                          LocalDate startDate,
                                                          LocalDate endDate);

    /**
     * Findet alle Reiseanträge, die zur Archivierung bereit sind (Status PAID)
     */
    List<TravelRequest> findAllReadyForArchiving();

    /**
     * Findet alle archivierten Reiseanträge, deren Aufbewahrungsfrist abgelaufen ist
     */
    List<TravelRequest> findAllWithExpiredRetention();

    /**
     * Findet archivierte Reiseanträge in einem Zeitraum
     */
    List<TravelRequest> findArchivedBetween(LocalDate startDate, LocalDate endDate);
}
