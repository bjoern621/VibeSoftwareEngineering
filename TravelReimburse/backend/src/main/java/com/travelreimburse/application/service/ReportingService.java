package com.travelreimburse.application.service;

import com.travelreimburse.application.dto.BudgetOverviewDTO;
import com.travelreimburse.application.dto.CostCenterReportDTO;
import com.travelreimburse.application.dto.TravelStatisticsResponseDTO;
import com.travelreimburse.domain.model.*;
import com.travelreimburse.domain.repository.TravelRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Application Service für HR-Reporting
 * Orchestriert Statistik-Erstellung aus Domain-Daten
 * 
 * DDD: Service ist Orchestrator, KEINE Business-Logik hier!
 * Business-Logik gehört in Value Objects (TravelStatistics, BudgetOverview, etc.)
 */
@Service
@Transactional(readOnly = true)
public class ReportingService {

    private final TravelRequestRepository travelRequestRepository;

    public ReportingService(TravelRequestRepository travelRequestRepository) {
        this.travelRequestRepository = travelRequestRepository;
    }

    /**
     * Generiert Reisestatistiken für einen Zeitraum
     * 
     * @param startDate Startdatum (optional, Standard: vor 1 Jahr)
     * @param endDate Enddatum (optional, Standard: heute)
     * @return Statistik-DTO für Präsentation
     */
    public TravelStatisticsResponseDTO getTravelStatistics(LocalDate startDate, LocalDate endDate) {
        // Default-Werte wenn nicht angegeben
        LocalDate effectiveStartDate = (startDate != null) ? startDate : LocalDate.now().minusYears(1);
        LocalDate effectiveEndDate = (endDate != null) ? endDate : LocalDate.now();

        // Domain-Repository abfragen (nach Erstellungsdatum für HR-Reporting!)
        List<TravelRequest> allRequests = travelRequestRepository.findByCreatedAtRange(
            effectiveStartDate, 
            effectiveEndDate
        );

        // Statistiken berechnen (Delegation an Helper-Methode)
        TravelStatistics statistics = calculateStatistics(
            allRequests, 
            effectiveStartDate, 
            effectiveEndDate
        );

        // Domain Value Object → DTO konvertieren
        return toStatisticsDTO(statistics);
    }

    /**
     * Generiert Budget-Übersicht
     * 
     * @param allocatedBudgetAmount zugewiesenes Budget
     * @param currency Währung
     * @param startDate Startdatum für Zeitraum (optional)
     * @param endDate Enddatum für Zeitraum (optional)
     * @return Budget-DTO für Präsentation
     */
    public BudgetOverviewDTO getBudgetOverview(BigDecimal allocatedBudgetAmount, 
                                               String currency,
                                               LocalDate startDate,
                                               LocalDate endDate) {
        
        // Validierung
        if (allocatedBudgetAmount == null || allocatedBudgetAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Zugewiesenes Budget muss positiv sein");
        }
        if (currency == null || currency.trim().isEmpty()) {
            throw new IllegalArgumentException("Währung darf nicht leer sein");
        }

        com.travelreimburse.domain.model.Currency budgetCurrency;
        try {
            budgetCurrency = com.travelreimburse.domain.model.Currency.valueOf(currency.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Ungültige Währung: " + currency);
        }

        // Default-Werte für Zeitraum
        LocalDate effectiveStartDate = (startDate != null) ? startDate : LocalDate.now().minusYears(1);
        LocalDate effectiveEndDate = (endDate != null) ? endDate : LocalDate.now();

        // Domain-Repository abfragen
        List<TravelRequest> approvedRequests = travelRequestRepository.findByStatusAndDateRange(
            TravelRequestStatus.APPROVED,
            effectiveStartDate,
            effectiveEndDate
        );

        // Verwendetes Budget berechnen (nur genehmigte Reisen)
        Money usedBudget = calculateTotalCost(approvedRequests, budgetCurrency);
        Money allocatedBudget = new Money(allocatedBudgetAmount, budgetCurrency);

        // Domain Value Object erstellen (Business-Logik in BudgetOverview!)
        BudgetOverview overview = new BudgetOverview(allocatedBudget, usedBudget);

        // Domain Value Object → DTO konvertieren
        return toBudgetOverviewDTO(overview);
    }

    /**
     * Generiert Kostenstellen-Reports
     * Gruppiert nach echten Kostenstellen (CostCenter)
     * 
     * @param startDate Startdatum (optional)
     * @param endDate Enddatum (optional)
     * @return Liste von Kostenstellen-Reports
     */
    public List<CostCenterReportDTO> getCostCenterReports(LocalDate startDate, LocalDate endDate) {
        
        // Default-Werte
        LocalDate effectiveStartDate = (startDate != null) ? startDate : LocalDate.now().minusYears(1);
        LocalDate effectiveEndDate = (endDate != null) ? endDate : LocalDate.now();

        // Alle Reiseanträge im Zeitraum holen (nach Erstellungsdatum!)
        List<TravelRequest> allRequests = travelRequestRepository.findByCreatedAtRange(
            effectiveStartDate,
            effectiveEndDate
        );

        // Nach CostCenter gruppieren (echte Kostenstellen!)
        Map<String, List<TravelRequest>> requestsByCostCenter = allRequests.stream()
            .filter(tr -> tr.getCostCenter() != null) // Filter null CostCenters
            .collect(Collectors.groupingBy(tr -> tr.getCostCenter().getCode()));

        // Für jede Kostenstelle einen Report erstellen
        return requestsByCostCenter.entrySet().stream()
            .map(entry -> createCostCenterReport(
                entry.getKey(), 
                entry.getValue()
            ))
            .sorted(Comparator.comparing(CostCenterReportDTO::totalCost).reversed())
            .toList();
    }

    // ========== PRIVATE HELPER METHODS (Orchestration) ==========

    /**
     * Berechnet Statistiken aus Reiseanträgen
     * DDD: Orchestration - delegiert an Domain Value Object
     */
    private TravelStatistics calculateStatistics(List<TravelRequest> requests, 
                                                 LocalDate startDate, 
                                                 LocalDate endDate) {
        
        // Nur Anträge mit Status != DRAFT zählen (HR-relevant)
        List<TravelRequest> relevant = requests.stream()
            .filter(r -> r.getStatus() != TravelRequestStatus.DRAFT)
            .toList();

        long totalRequests = relevant.size();
        long approvedRequests = relevant.stream()
            .filter(r -> r.getStatus() == TravelRequestStatus.APPROVED)
            .count();
        long rejectedRequests = relevant.stream()
            .filter(r -> r.getStatus() == TravelRequestStatus.REJECTED)
            .count();
        long pendingRequests = relevant.stream()
            .filter(r -> r.getStatus() == TravelRequestStatus.SUBMITTED)
            .count();

        // Gesamtkosten berechnen (in EUR als Standard)
        Money totalCost = calculateTotalCost(relevant, com.travelreimburse.domain.model.Currency.EUR);

        // Durchschnittskosten
        Money avgCost;
        if (totalRequests > 0) {
            BigDecimal avgAmount = totalCost.getAmount()
                .divide(BigDecimal.valueOf(totalRequests), 2, RoundingMode.HALF_UP);
            avgCost = new Money(avgAmount, com.travelreimburse.domain.model.Currency.EUR);
        } else {
            avgCost = new Money(BigDecimal.ZERO, com.travelreimburse.domain.model.Currency.EUR);
        }

        // Gesamte Reisetage
        long totalDays = relevant.stream()
            .mapToLong(r -> r.getTravelPeriod().getDays())
            .sum();

        // Domain Value Object erstellen (Business-Logik in TravelStatistics!)
        return new TravelStatistics(
            startDate,
            endDate,
            totalRequests,
            approvedRequests,
            rejectedRequests,
            pendingRequests,
            totalCost,
            avgCost,
            totalDays
        );
    }

    /**
     * Berechnet Gesamtkosten aus Reiseanträgen in gewünschter Währung
     * TODO: In Realität würde hier der ExRat-Service für Währungsumrechnung genutzt
     */
    private Money calculateTotalCost(List<TravelRequest> requests, 
                                    com.travelreimburse.domain.model.Currency targetCurrency) {
        BigDecimal total = requests.stream()
            .filter(r -> r.getEstimatedCost().getCurrency() == targetCurrency)
            .map(r -> r.getEstimatedCost().getAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return new Money(total, targetCurrency);
    }

    /**
     * Erstellt Kostenstellen-Report für eine Kostenstelle
     * Verwendet echte CostCenter-Daten aus dem Domain Model
     */
    private CostCenterReportDTO createCostCenterReport(String costCenterCode, 
                                                       List<TravelRequest> requests) {
        
        // Nimm den CostCenter-Namen vom ersten Request (alle haben denselben)
        String costCenterName = requests.isEmpty() ? "Unknown" 
            : requests.get(0).getCostCenter().getName();
        
        long travelCount = requests.size();
        
        Money totalCost = calculateTotalCost(requests, com.travelreimburse.domain.model.Currency.EUR);
        
        Money avgCost;
        if (travelCount > 0) {
            BigDecimal avgAmount = totalCost.getAmount()
                .divide(BigDecimal.valueOf(travelCount), 2, RoundingMode.HALF_UP);
            avgCost = new Money(avgAmount, com.travelreimburse.domain.model.Currency.EUR);
        } else {
            avgCost = new Money(BigDecimal.ZERO, com.travelreimburse.domain.model.Currency.EUR);
        }

        // Anzahl unterschiedlicher Employees in dieser Kostenstelle
        long employeeCount = requests.stream()
            .map(TravelRequest::getEmployeeId)
            .distinct()
            .count();

        // Domain Value Object erstellen (Business-Logik in CostCenterReport!)
        CostCenterReport report = new CostCenterReport(
            costCenterCode,
            costCenterName,
            travelCount,
            totalCost,
            avgCost,
            employeeCount
        );

        // Domain → DTO
        return toCostCenterReportDTO(report);
    }

    // ========== DTO CONVERSION (Domain → Presentation) ==========

    /**
     * Konvertiert TravelStatistics (Domain) → TravelStatisticsResponseDTO (Presentation)
     * DDD: DTOs nur für Datenübertragung, keine Logik!
     */
    private TravelStatisticsResponseDTO toStatisticsDTO(TravelStatistics statistics) {
        return new TravelStatisticsResponseDTO(
            statistics.getPeriodStart().toString(),
            statistics.getPeriodEnd().toString(),
            statistics.getTotalRequests(),
            statistics.getApprovedRequests(),
            statistics.getRejectedRequests(),
            statistics.getPendingRequests(),
            statistics.getTotalEstimatedCost().getAmount(),
            statistics.getTotalEstimatedCost().getCurrency().name(),
            statistics.getAverageCostPerTrip().getAmount(),
            statistics.getTotalTravelDays(),
            statistics.getApprovalRate(), // Business-Logik in Domain!
            statistics.getRejectionRate()  // Business-Logik in Domain!
        );
    }

    /**
     * Konvertiert BudgetOverview (Domain) → BudgetOverviewDTO (Presentation)
     */
    private BudgetOverviewDTO toBudgetOverviewDTO(BudgetOverview overview) {
        return new BudgetOverviewDTO(
            overview.getAllocatedBudget().getAmount(),
            overview.getUsedBudget().getAmount(),
            overview.getRemainingBudget().getAmount(),
            overview.getAllocatedBudget().getCurrency().name(),
            overview.isBudgetExceeded(),
            overview.getOverrunAmount().getAmount(),
            overview.getUtilizationPercentage(), // Business-Logik in Domain!
            overview.isCriticalUtilization()      // Business-Logik in Domain!
        );
    }

    /**
     * Konvertiert CostCenterReport (Domain) → CostCenterReportDTO (Presentation)
     */
    private CostCenterReportDTO toCostCenterReportDTO(CostCenterReport report) {
        return new CostCenterReportDTO(
            report.getCostCenterCode(),
            report.getCostCenterName(),
            report.getTravelRequestCount(),
            report.getTotalCost().getAmount(),
            report.getTotalCost().getCurrency().name(),
            report.getAverageCostPerTrip().getAmount(),
            report.getEmployeeCount(),
            report.getAverageTravelsPerEmployee(), // Business-Logik in Domain!
            report.isActive()                       // Business-Logik in Domain!
        );
    }
}
