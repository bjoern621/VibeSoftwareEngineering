package com.rentacar.presentation.dto;

/**
 * DTO für Fahrzeuginformationen in der Buchungshistorie.
 * 
 * Enthält kompakte Fahrzeug-Informationen zur Anzeige in der Buchungsliste.
 */
public record FahrzeugInfoDto(
    Long id,
    String kennzeichen,
    String marke,
    String modell,
    String fahrzeugtyp
) {}
