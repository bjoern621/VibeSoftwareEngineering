package com.travelreimburse.domain.exception;

/**
 * Exception für ungültige Eingabedaten bei Erstellung/Update von TravelRequests
 */
public class InvalidTravelRequestDataException extends DomainException {
    
    public InvalidTravelRequestDataException(String fieldName) {
        super(String.format("Ungültige Eingabe für Feld: %s", fieldName));
    }
    
    public InvalidTravelRequestDataException(String fieldName, String reason) {
        super(String.format("Ungültige Eingabe für Feld '%s': %s", fieldName, reason));
    }
}
