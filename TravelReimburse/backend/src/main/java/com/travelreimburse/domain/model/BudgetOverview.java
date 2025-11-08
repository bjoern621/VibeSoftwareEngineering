package com.travelreimburse.domain.model;

import java.util.Objects;

/**
 * Value Object für Budget-Übersicht
 * 
 * DDD: Immutable Value Object - keine Identität
 * Repräsentiert Budget-Informationen und -Überschreitungen
 */
public class BudgetOverview {

    private final Money allocatedBudget;
    private final Money usedBudget;
    private final Money remainingBudget;
    private final boolean budgetExceeded;
    private final Money overrunAmount;

    /**
     * Konstruktor mit Validierung und Berechnung
     * DDD: Invarianten werden hier geprüft
     */
    public BudgetOverview(Money allocatedBudget, Money usedBudget) {
        if (allocatedBudget == null) {
            throw new IllegalArgumentException("Zugewiesenes Budget darf nicht null sein");
        }
        if (usedBudget == null) {
            throw new IllegalArgumentException("Verwendetes Budget darf nicht null sein");
        }
        if (!allocatedBudget.getCurrency().equals(usedBudget.getCurrency())) {
            throw new IllegalArgumentException("Währungen müssen übereinstimmen");
        }

        this.allocatedBudget = allocatedBudget;
        this.usedBudget = usedBudget;
        
        // Berechne verbleibendes Budget
        if (usedBudget.isGreaterThan(allocatedBudget)) {
            // Budget überschritten
            this.budgetExceeded = true;
            this.remainingBudget = new Money(java.math.BigDecimal.ZERO, allocatedBudget.getCurrency());
            // Overrun = used - allocated (hier ist used > allocated, also positiv)
            this.overrunAmount = new Money(
                usedBudget.getAmount().subtract(allocatedBudget.getAmount()),
                allocatedBudget.getCurrency()
            );
        } else {
            // Budget eingehalten
            this.budgetExceeded = false;
            this.remainingBudget = allocatedBudget.subtract(usedBudget);
            this.overrunAmount = new Money(java.math.BigDecimal.ZERO, allocatedBudget.getCurrency());
        }
    }

    /**
     * Business-Methode: Berechnet die Budget-Auslastung als Prozentsatz
     * @return Auslastung in Prozent (0-100+, kann über 100 sein bei Überschreitung)
     */
    public double getUtilizationPercentage() {
        if (allocatedBudget.getAmount().compareTo(java.math.BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return usedBudget.getAmount()
                .divide(allocatedBudget.getAmount(), 4, java.math.RoundingMode.HALF_UP)
                .multiply(java.math.BigDecimal.valueOf(100))
                .doubleValue();
    }

    /**
     * Business-Methode: Prüft ob Budget kritisch ausgelastet ist (>80%)
     * @return true wenn kritische Auslastung, sonst false
     */
    public boolean isCriticalUtilization() {
        return getUtilizationPercentage() >= 80.0;
    }

    /**
     * Business-Methode: Prüft ob noch genug Budget für neue Reise verfügbar ist
     * @param requestedAmount gewünschter Betrag
     * @return true wenn Budget verfügbar, sonst false
     */
    public boolean canAfford(Money requestedAmount) {
        if (requestedAmount == null) {
            throw new IllegalArgumentException("Angefragter Betrag darf nicht null sein");
        }
        if (!requestedAmount.getCurrency().equals(allocatedBudget.getCurrency())) {
            throw new IllegalArgumentException("Währungen müssen übereinstimmen");
        }
        
        if (budgetExceeded) {
            return false; // Budget bereits überschritten
        }
        
        return !requestedAmount.isGreaterThan(remainingBudget);
    }

    // Immutable - nur Getter, keine Setter!

    public Money getAllocatedBudget() {
        return allocatedBudget;
    }

    public Money getUsedBudget() {
        return usedBudget;
    }

    public Money getRemainingBudget() {
        return remainingBudget;
    }

    public boolean isBudgetExceeded() {
        return budgetExceeded;
    }

    public Money getOverrunAmount() {
        return overrunAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BudgetOverview that = (BudgetOverview) o;
        return budgetExceeded == that.budgetExceeded &&
               Objects.equals(allocatedBudget, that.allocatedBudget) &&
               Objects.equals(usedBudget, that.usedBudget) &&
               Objects.equals(remainingBudget, that.remainingBudget) &&
               Objects.equals(overrunAmount, that.overrunAmount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allocatedBudget, usedBudget, remainingBudget, 
                          budgetExceeded, overrunAmount);
    }

    @Override
    public String toString() {
        return "BudgetOverview{" +
               "allocated=" + allocatedBudget +
               ", used=" + usedBudget +
               ", remaining=" + remainingBudget +
               ", exceeded=" + budgetExceeded +
               ", overrun=" + overrunAmount +
               ", utilization=" + String.format("%.2f%%", getUtilizationPercentage()) +
               '}';
    }
}
