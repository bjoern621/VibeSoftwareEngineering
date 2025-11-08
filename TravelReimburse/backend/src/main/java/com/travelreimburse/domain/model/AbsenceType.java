package com.travelreimburse.domain.model;

/**
 * Typ einer Abwesenheit im HRIS
 */
public enum AbsenceType {
    VACATION,           // Urlaub
    SICK_LEAVE,         // Krankheit
    PARENTAL_LEAVE,     // Elternzeit
    UNPAID_LEAVE,       // Unbezahlter Urlaub
    BUSINESS_TRIP,      // Dienstreise (von HRIS gemeldet)
    OTHER               // Sonstiges
}
