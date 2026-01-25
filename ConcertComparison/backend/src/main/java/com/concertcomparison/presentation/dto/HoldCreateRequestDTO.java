package com.concertcomparison.presentation.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO für Hold-Erstellung.
 * OpenAPI: POST /seats/{id}/hold
 */
public record HoldCreateRequestDTO(
    @NotBlank(message = "UserId darf nicht leer sein")
    String userId
) {}
