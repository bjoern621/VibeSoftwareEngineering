package com.travelreimburse.domain.exception;

/**
 * Domain Exception für nicht gefundene Reiseanträge.
 * DDD: Domain-spezifische Exception im Domain Layer.
 */
public class TravelRequestNotFoundException extends RuntimeException {

    private final Long travelRequestId;

    public TravelRequestNotFoundException(Long id) {
        super("Reiseantrag mit ID " + id + " nicht gefunden");
        this.travelRequestId = id;
    }

    public TravelRequestNotFoundException(Long id, String additionalMessage) {
        super("Reiseantrag mit ID " + id + " nicht gefunden: " + additionalMessage);
        this.travelRequestId = id;
    }

    public Long getTravelRequestId() {
        return travelRequestId;
    }
}

