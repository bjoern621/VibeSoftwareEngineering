package com.travelreimburse.domain.service;

import com.travelreimburse.domain.model.AbsenceInfo;
import com.travelreimburse.domain.model.DateRange;
import com.travelreimburse.infrastructure.external.hris.HrisClient;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Domain Service für Abwesenheits-Validierung
 * Prüft ob ein Reisezeitraum mit bestehenden Abwesenheiten kollidiert
 */
@Service
public class AbsenceValidationService {

    private final HrisClient hrisClient;

    public AbsenceValidationService(HrisClient hrisClient) {
        this.hrisClient = hrisClient;
    }

    /**
     * Validiert einen Reisezeitraum gegen HRIS-Abwesenheiten
     * 
     * @param employeeId ID des Mitarbeiters
     * @param travelPeriod Reisezeitraum
     * @return Liste der konfligierenden Abwesenheiten (leer wenn keine Konflikte)
     */
    public List<AbsenceInfo> validateTravelAgainstAbsences(Long employeeId, DateRange travelPeriod) {
        if (employeeId == null) {
            throw new IllegalArgumentException("EmployeeId darf nicht null sein");
        }
        if (travelPeriod == null) {
            throw new IllegalArgumentException("TravelPeriod darf nicht null sein");
        }

        // Hole alle Abwesenheiten vom HRIS
        List<AbsenceInfo> absences = hrisClient.getAbsences(
            employeeId,
            travelPeriod.getStartDate(),
            travelPeriod.getEndDate()
        );

        // Filtere nur relevante Abwesenheiten (APPROVED und nicht BUSINESS_TRIP)
        // und prüfe auf Überlappungen
        return absences.stream()
            .filter(AbsenceInfo::isRelevantForValidation)
            .filter(absence -> absence.overlaps(travelPeriod))
            .toList();
    }
}
