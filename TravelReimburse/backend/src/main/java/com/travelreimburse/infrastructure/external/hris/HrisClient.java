package com.travelreimburse.infrastructure.external.hris;

import com.travelreimburse.domain.model.AbsenceInfo;
import com.travelreimburse.domain.model.AbsenceStatus;
import com.travelreimburse.domain.model.AbsenceType;
import com.travelreimburse.domain.model.DateRange;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Mock-Implementierung des HRIS-Clients für Entwicklung und Tests
 * Liefert vordefinierte Test-Daten
 */
@Component
public class HrisClient {

    // Test-Daten: Employee -> Liste von Abwesenheiten
    private static final Map<Long, List<AbsenceInfo>> MOCK_DATA = Map.of(
        // Employee 1: Hat Urlaub vom 10.11-15.11.2025
        1L, List.of(
            new AbsenceInfo(
                "ABS-001",
                AbsenceType.VACATION,
                new DateRange(LocalDate.of(2025, 11, 10), LocalDate.of(2025, 11, 15)),
                AbsenceStatus.APPROVED,
                "Jahresurlaub"
            )
        ),
        
        // Employee 2: Hat Krankheit vom 08.11-09.11.2025
        2L, List.of(
            new AbsenceInfo(
                "ABS-002",
                AbsenceType.SICK_LEAVE,
                new DateRange(LocalDate.of(2025, 11, 8), LocalDate.of(2025, 11, 9)),
                AbsenceStatus.APPROVED,
                "Erkältung"
            )
        ),
        
        // Employee 3: Keine Abwesenheiten
        3L, List.of()
    );

    /**
     * Ruft Abwesenheiten eines Mitarbeiters für einen Zeitraum ab
     * 
     * @param employeeId ID des Mitarbeiters
     * @param startDate Start des Zeitraums
     * @param endDate Ende des Zeitraums
     * @return Liste der Abwesenheiten (kann leer sein)
     */
    public List<AbsenceInfo> getAbsences(Long employeeId, LocalDate startDate, LocalDate endDate) {
        if (employeeId == null) {
            throw new IllegalArgumentException("EmployeeId darf nicht null sein");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start- und Enddatum dürfen nicht null sein");
        }
        
        // Hole alle Abwesenheiten für diesen Employee
        List<AbsenceInfo> allAbsences = MOCK_DATA.getOrDefault(employeeId, List.of());
        
        // Filtere nur Abwesenheiten, die im angefragten Zeitraum liegen
        DateRange requestedPeriod = new DateRange(startDate, endDate);
        
        return allAbsences.stream()
            .filter(absence -> absence.overlaps(requestedPeriod))
            .toList();
    }
}
