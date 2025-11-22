package com.rentacar.presentation.dto;

import com.rentacar.domain.model.AdditionalServiceType;
import com.rentacar.domain.model.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO für Buchungshistorie-Anzeige.
 * 
 * Enthält alle relevanten Informationen einer Buchung für die Anzeige.
 */
public record BookingHistoryDto(
    Long buchungsnummer,
    FahrzeugInfoDto fahrzeug,
    FilialeInfoDto abholfiliale,
    FilialeInfoDto rueckgabefiliale,
    LocalDateTime abholdatum,
    LocalDateTime rueckgabedatum,
    BookingStatus status,
    BigDecimal gesamtpreis,
    String waehrung,
    Set<AdditionalServiceType> zusatzleistungen,
    LocalDateTime erstelltAm,
    String stornierungsgrund
) {}
