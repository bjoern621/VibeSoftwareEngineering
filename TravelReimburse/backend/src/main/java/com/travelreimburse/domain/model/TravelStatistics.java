package com.travelreimburse.domain.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Value Object für Reisestatistiken
 * 
 * DDD: Immutable Value Object - keine Identität, keine Setter
 * Repräsentiert aggregierte Statistiken über Reiseaktivitäten
 */
public class TravelStatistics {

    private final LocalDate periodStart;
    private final LocalDate periodEnd;
    private final long totalRequests;
    private final long approvedRequests;
    private final long rejectedRequests;
    private final long pendingRequests;
    private final Money totalEstimatedCost;
    private final Money averageCostPerTrip;
    private final long totalTravelDays;

    /**
     * Konstruktor mit Validierung
     * DDD: Invarianten werden hier geprüft
     */
    public TravelStatistics(LocalDate periodStart, 
                           LocalDate periodEnd,
                           long totalRequests,
                           long approvedRequests,
                           long rejectedRequests,
                           long pendingRequests,
                           Money totalEstimatedCost,
                           Money averageCostPerTrip,
                           long totalTravelDays) {
        
        if (periodStart == null) {
            throw new IllegalArgumentException("Startdatum darf nicht null sein");
        }
        if (periodEnd == null) {
            throw new IllegalArgumentException("Enddatum darf nicht null sein");
        }
        if (periodEnd.isBefore(periodStart)) {
            throw new IllegalArgumentException("Enddatum muss nach Startdatum liegen");
        }
        if (totalRequests < 0) {
            throw new IllegalArgumentException("Anzahl Reiseanträge darf nicht negativ sein");
        }
        if (approvedRequests < 0 || rejectedRequests < 0 || pendingRequests < 0) {
            throw new IllegalArgumentException("Status-Zähler dürfen nicht negativ sein");
        }
        if (totalEstimatedCost == null) {
            throw new IllegalArgumentException("Gesamtkosten dürfen nicht null sein");
        }
        if (averageCostPerTrip == null) {
            throw new IllegalArgumentException("Durchschnittskosten dürfen nicht null sein");
        }
        if (totalTravelDays < 0) {
            throw new IllegalArgumentException("Reisetage dürfen nicht negativ sein");
        }

        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.totalRequests = totalRequests;
        this.approvedRequests = approvedRequests;
        this.rejectedRequests = rejectedRequests;
        this.pendingRequests = pendingRequests;
        this.totalEstimatedCost = totalEstimatedCost;
        this.averageCostPerTrip = averageCostPerTrip;
        this.totalTravelDays = totalTravelDays;
    }

    /**
     * Business-Methode: Berechnet die Genehmigungsquote
     * @return Genehmigungsquote als Prozentsatz (0-100)
     */
    public double getApprovalRate() {
        if (totalRequests == 0) {
            return 0.0;
        }
        return (approvedRequests * 100.0) / totalRequests;
    }

    /**
     * Business-Methode: Berechnet die Ablehnungsquote
     * @return Ablehnungsquote als Prozentsatz (0-100)
     */
    public double getRejectionRate() {
        if (totalRequests == 0) {
            return 0.0;
        }
        return (rejectedRequests * 100.0) / totalRequests;
    }

    /**
     * Business-Methode: Prüft ob Budget-Limit überschritten wird
     * @param budgetLimit das vorgegebene Budget
     * @return true wenn Limit überschritten, sonst false
     */
    public boolean exceedsBudget(Money budgetLimit) {
        if (budgetLimit == null) {
            throw new IllegalArgumentException("Budget-Limit darf nicht null sein");
        }
        if (!totalEstimatedCost.getCurrency().equals(budgetLimit.getCurrency())) {
            throw new IllegalArgumentException("Währungen müssen übereinstimmen");
        }
        return totalEstimatedCost.isGreaterThan(budgetLimit);
    }

    // Immutable - nur Getter, keine Setter!
    
    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public long getTotalRequests() {
        return totalRequests;
    }

    public long getApprovedRequests() {
        return approvedRequests;
    }

    public long getRejectedRequests() {
        return rejectedRequests;
    }

    public long getPendingRequests() {
        return pendingRequests;
    }

    public Money getTotalEstimatedCost() {
        return totalEstimatedCost;
    }

    public Money getAverageCostPerTrip() {
        return averageCostPerTrip;
    }

    public long getTotalTravelDays() {
        return totalTravelDays;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TravelStatistics that = (TravelStatistics) o;
        return totalRequests == that.totalRequests &&
               approvedRequests == that.approvedRequests &&
               rejectedRequests == that.rejectedRequests &&
               pendingRequests == that.pendingRequests &&
               totalTravelDays == that.totalTravelDays &&
               Objects.equals(periodStart, that.periodStart) &&
               Objects.equals(periodEnd, that.periodEnd) &&
               Objects.equals(totalEstimatedCost, that.totalEstimatedCost) &&
               Objects.equals(averageCostPerTrip, that.averageCostPerTrip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periodStart, periodEnd, totalRequests, approvedRequests, 
                          rejectedRequests, pendingRequests, totalEstimatedCost, 
                          averageCostPerTrip, totalTravelDays);
    }

    @Override
    public String toString() {
        return "TravelStatistics{" +
               "period=" + periodStart + " bis " + periodEnd +
               ", totalRequests=" + totalRequests +
               ", approvedRequests=" + approvedRequests +
               ", rejectedRequests=" + rejectedRequests +
               ", pendingRequests=" + pendingRequests +
               ", totalCost=" + totalEstimatedCost +
               ", avgCost=" + averageCostPerTrip +
               ", totalDays=" + totalTravelDays +
               '}';
    }
}
