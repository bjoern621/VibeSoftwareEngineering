package com.travelreimburse.domain.model;

import java.util.Objects;

/**
 * Value Object für Kostenstellen-Report
 * 
 * DDD: Immutable Value Object - keine Identität
 * Repräsentiert Reisekosten-Statistiken pro Kostenstelle
 */
public class CostCenterReport {

    private final String costCenterCode;
    private final String costCenterName;
    private final long travelRequestCount;
    private final Money totalCost;
    private final Money averageCostPerTrip;
    private final long employeeCount;

    /**
     * Konstruktor mit Validierung
     * DDD: Invarianten werden hier geprüft
     */
    public CostCenterReport(String costCenterCode,
                           String costCenterName,
                           long travelRequestCount,
                           Money totalCost,
                           Money averageCostPerTrip,
                           long employeeCount) {
        
        if (costCenterCode == null || costCenterCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Kostenstellen-Code darf nicht leer sein");
        }
        if (costCenterName == null || costCenterName.trim().isEmpty()) {
            throw new IllegalArgumentException("Kostenstellen-Name darf nicht leer sein");
        }
        if (travelRequestCount < 0) {
            throw new IllegalArgumentException("Anzahl Reiseanträge darf nicht negativ sein");
        }
        if (totalCost == null) {
            throw new IllegalArgumentException("Gesamtkosten dürfen nicht null sein");
        }
        if (averageCostPerTrip == null) {
            throw new IllegalArgumentException("Durchschnittskosten dürfen nicht null sein");
        }
        if (employeeCount < 0) {
            throw new IllegalArgumentException("Mitarbeiter-Anzahl darf nicht negativ sein");
        }

        this.costCenterCode = costCenterCode;
        this.costCenterName = costCenterName;
        this.travelRequestCount = travelRequestCount;
        this.totalCost = totalCost;
        this.averageCostPerTrip = averageCostPerTrip;
        this.employeeCount = employeeCount;
    }

    /**
     * Business-Methode: Berechnet durchschnittliche Reisen pro Mitarbeiter
     * @return Durchschnitt der Reisen pro Mitarbeiter
     */
    public double getAverageTravelsPerEmployee() {
        if (employeeCount == 0) {
            return 0.0;
        }
        return (double) travelRequestCount / employeeCount;
    }

    /**
     * Business-Methode: Prüft ob Kostenstelle über Durchschnitt liegt
     * @param globalAverage globaler Durchschnitt der Reisekosten
     * @return true wenn über Durchschnitt, sonst false
     */
    public boolean isAboveAverage(Money globalAverage) {
        if (globalAverage == null) {
            throw new IllegalArgumentException("Globaler Durchschnitt darf nicht null sein");
        }
        if (!totalCost.getCurrency().equals(globalAverage.getCurrency())) {
            throw new IllegalArgumentException("Währungen müssen übereinstimmen");
        }
        return averageCostPerTrip.isGreaterThan(globalAverage);
    }

    /**
     * Business-Methode: Prüft ob Kostenstelle aktiv ist (mind. 1 Reise)
     * @return true wenn aktiv, sonst false
     */
    public boolean isActive() {
        return travelRequestCount > 0;
    }

    // Immutable - nur Getter, keine Setter!

    public String getCostCenterCode() {
        return costCenterCode;
    }

    public String getCostCenterName() {
        return costCenterName;
    }

    public long getTravelRequestCount() {
        return travelRequestCount;
    }

    public Money getTotalCost() {
        return totalCost;
    }

    public Money getAverageCostPerTrip() {
        return averageCostPerTrip;
    }

    public long getEmployeeCount() {
        return employeeCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CostCenterReport that = (CostCenterReport) o;
        return travelRequestCount == that.travelRequestCount &&
               employeeCount == that.employeeCount &&
               Objects.equals(costCenterCode, that.costCenterCode) &&
               Objects.equals(costCenterName, that.costCenterName) &&
               Objects.equals(totalCost, that.totalCost) &&
               Objects.equals(averageCostPerTrip, that.averageCostPerTrip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(costCenterCode, costCenterName, travelRequestCount, 
                          totalCost, averageCostPerTrip, employeeCount);
    }

    @Override
    public String toString() {
        return "CostCenterReport{" +
               "code='" + costCenterCode + '\'' +
               ", name='" + costCenterName + '\'' +
               ", requests=" + travelRequestCount +
               ", totalCost=" + totalCost +
               ", avgCost=" + averageCostPerTrip +
               ", employees=" + employeeCount +
               ", avgPerEmployee=" + String.format("%.2f", getAverageTravelsPerEmployee()) +
               '}';
    }
}
