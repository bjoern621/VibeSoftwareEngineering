package com.rentacar.domain.service;

import com.rentacar.domain.model.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Domain Service für die Berechnung von Zusatzkosten.
 */
public class AdditionalCostService {

    private static final BigDecimal LATE_FEE_PER_HOUR = new BigDecimal("20.00");
    private static final BigDecimal EXCESS_MILEAGE_RATE = new BigDecimal("0.30");

    /**
     * Berechnet die Zusatzkosten für eine Miete.
     *
     * @param booking Die Buchung
     * @param rentalAgreement Der Mietvertrag
     * @param damageReports Liste der Schadensberichte
     * @return Berechnete Zusatzkosten
     */
    public AdditionalCosts calculateAdditionalCosts(Booking booking, RentalAgreement rentalAgreement, List<DamageReport> damageReports) {
        Objects.requireNonNull(booking, "Booking must not be null");
        Objects.requireNonNull(rentalAgreement, "RentalAgreement must not be null");

        BigDecimal lateFee = calculateLateFee(booking.getReturnDateTime(), rentalAgreement.getCheckinTime());
        BigDecimal excessMileageFee = calculateExcessMileageFee(booking, rentalAgreement);
        BigDecimal damageCost = calculateDamageCost(damageReports);

        return new AdditionalCosts(lateFee, excessMileageFee, damageCost);
    }

    private BigDecimal calculateLateFee(LocalDateTime scheduledReturn, LocalDateTime actualReturn) {
        if (actualReturn == null || !actualReturn.isAfter(scheduledReturn)) {
            return BigDecimal.ZERO;
        }
        
        long minutesLate = Duration.between(scheduledReturn, actualReturn).toMinutes();
        if (minutesLate <= 0) return BigDecimal.ZERO;

        long hoursLate = (minutesLate + 59) / 60; // Aufrunden auf angefangene Stunde
        return LATE_FEE_PER_HOUR.multiply(BigDecimal.valueOf(hoursLate));
    }

    private BigDecimal calculateExcessMileageFee(Booking booking, RentalAgreement rentalAgreement) {
        if (rentalAgreement.getCheckinMileage() == null || rentalAgreement.getCheckoutMileage() == null) {
            return BigDecimal.ZERO;
        }

        int driven = rentalAgreement.getCheckinMileage().getKilometers() - rentalAgreement.getCheckoutMileage().getKilometers();
        int included = booking.getIncludedKilometers() != null ? booking.getIncludedKilometers() : 0;

        if (driven > included) {
            int excess = driven - included;
            return EXCESS_MILEAGE_RATE.multiply(BigDecimal.valueOf(excess));
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateDamageCost(List<DamageReport> damageReports) {
        if (damageReports == null || damageReports.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return damageReports.stream()
                .map(DamageReport::getEstimatedCost)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
