package com.rentacar.domain.model;

/**
 * Status eines Mietvertrags.
 */
public enum RentalAgreementStatus {
    /** Mietvertrag ist aktiv (Fahrzeug ausgegeben) */
    OPEN,
    
    /** Mietvertrag ist abgeschlossen (Fahrzeug zurückgegeben) */
    CLOSED
}
