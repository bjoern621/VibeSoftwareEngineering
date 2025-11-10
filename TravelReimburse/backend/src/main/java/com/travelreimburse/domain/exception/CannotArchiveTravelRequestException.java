package com.travelreimburse.domain.exception;

/**
 * Exception wenn Reiseantrag nicht archiviert werden kann
 */
public class CannotArchiveTravelRequestException extends RuntimeException {
    
    private final String reason;
    
    public CannotArchiveTravelRequestException(Long requestId, String reason) {
        super(String.format("Reiseantrag %d kann nicht archiviert werden: %s", requestId, reason));
        this.reason = reason;
    }
    
    public String getReason() {
        return reason;
    }
}

