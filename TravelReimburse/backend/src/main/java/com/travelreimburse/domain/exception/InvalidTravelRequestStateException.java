package com.travelreimburse.domain.exception;

import com.travelreimburse.domain.model.TravelRequestStatus;

/**
 * Exception für ungültige Zustandsübergänge
 * Wird geworfen, wenn eine Business-Operation im aktuellen Status nicht erlaubt ist
 */
public class InvalidTravelRequestStateException extends DomainException {
    
    public InvalidTravelRequestStateException(TravelRequestStatus currentStatus, String operation) {
        super(String.format(
            "Operation '%s' ist im Status '%s' nicht erlaubt", 
            operation, 
            currentStatus
        ));
    }
}
