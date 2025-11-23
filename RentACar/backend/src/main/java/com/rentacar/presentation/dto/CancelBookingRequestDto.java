package com.rentacar.presentation.dto;

import jakarta.validation.constraints.Size;

/**
 * Request DTO für Buchungsstornierung.
 * 
 * Stornierungsgrund ist optional.
 */
public record CancelBookingRequestDto(
    
    @Size(max = 500, message = "Stornierungsgrund darf maximal 500 Zeichen lang sein")
    String reason
) {}

