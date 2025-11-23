package com.rentacar.presentation.dto;

import java.math.BigDecimal;

public class AdditionalCostsDTO {
    private BigDecimal lateFee;
    private BigDecimal excessMileageFee;
    private BigDecimal damageCost;
    private BigDecimal totalAdditionalCost;

    public AdditionalCostsDTO(BigDecimal lateFee, BigDecimal excessMileageFee, BigDecimal damageCost, BigDecimal totalAdditionalCost) {
        this.lateFee = lateFee;
        this.excessMileageFee = excessMileageFee;
        this.damageCost = damageCost;
        this.totalAdditionalCost = totalAdditionalCost;
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
}
