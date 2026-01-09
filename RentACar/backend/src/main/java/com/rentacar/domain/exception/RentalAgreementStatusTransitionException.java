package com.rentacar.domain.exception;

import com.rentacar.domain.model.RentalAgreementStatus;

/**
 * Exception für ungültige Statusübergänge bei Mietverträgen.
 */
public class RentalAgreementStatusTransitionException extends RuntimeException {
    
    public RentalAgreementStatusTransitionException(Long id, RentalAgreementStatus currentStatus, RentalAgreementStatus targetStatus) {
        super(String.format("Ungültiger Statusübergang für Mietvertrag %d: %s -> %s", id, currentStatus, targetStatus));
    }
}
