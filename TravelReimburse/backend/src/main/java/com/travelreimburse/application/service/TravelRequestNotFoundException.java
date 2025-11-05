package com.travelreimburse.application.service;

/**
 * Exception wenn ein Reiseantrag nicht gefunden wurde
 */
public class TravelRequestNotFoundException extends RuntimeException {
    
    public TravelRequestNotFoundException(Long id) {
        super("Reiseantrag mit ID " + id + " nicht gefunden");
    }
}
