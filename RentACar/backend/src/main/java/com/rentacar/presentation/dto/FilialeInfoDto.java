package com.rentacar.presentation.dto;

/**
 * DTO für Filialinformationen in der Buchungshistorie.
 * 
 * Enthält kompakte Filial-Informationen zur Anzeige in der Buchungsliste.
 */
public record FilialeInfoDto(
    Long id,
    String name,
    String adresse
) {}
