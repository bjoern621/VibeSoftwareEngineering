package com.rentacar.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object für die Aufschlüsselung der Zusatzkosten.
 */
@Embeddable
public class AdditionalCosts {

    @Column(name = "late_fee")
    private BigDecimal lateFee;

    @Column(name = "excess_mileage_fee")
    private BigDecimal excessMileageFee;

    @Column(name = "damage_cost")
    private BigDecimal damageCost;

    @Column(name = "total_additional_cost")
    private BigDecimal totalAdditionalCost;

    protected AdditionalCosts() {
        // JPA
    }

    public AdditionalCosts(BigDecimal lateFee, BigDecimal excessMileageFee, BigDecimal damageCost) {
        this.lateFee = lateFee != null ? lateFee : BigDecimal.ZERO;
        this.excessMileageFee = excessMileageFee != null ? excessMileageFee : BigDecimal.ZERO;
        this.damageCost = damageCost != null ? damageCost : BigDecimal.ZERO;
        this.totalAdditionalCost = this.lateFee.add(this.excessMileageFee).add(this.damageCost);
    }

    public static AdditionalCosts zero() {
        return new AdditionalCosts(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public BigDecimal getLateFee() {
        return lateFee;
    }

    public BigDecimal getExcessMileageFee() {
        return excessMileageFee;
    }

    public BigDecimal getDamageCost() {
        return damageCost;
    }

    public BigDecimal getTotalAdditionalCost() {
        return totalAdditionalCost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdditionalCosts that = (AdditionalCosts) o;
        return Objects.equals(lateFee, that.lateFee) &&
               Objects.equals(excessMileageFee, that.excessMileageFee) &&
               Objects.equals(damageCost, that.damageCost) &&
               Objects.equals(totalAdditionalCost, that.totalAdditionalCost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lateFee, excessMileageFee, damageCost, totalAdditionalCost);
    }

    @Override
    public String toString() {
        return "AdditionalCosts{" +
               "lateFee=" + lateFee +
               ", excessMileageFee=" + excessMileageFee +
               ", damageCost=" + damageCost +
               ", totalAdditionalCost=" + totalAdditionalCost +
               '}';
    }
}
