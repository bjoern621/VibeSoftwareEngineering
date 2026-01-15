package com.concertcomparison.presentation.dto;

import java.time.LocalDateTime;

/**
 * Response DTO für Hold-Erstellung.
 * OpenAPI: HoldResponse
 */
public record HoldResponseDTO(
    String holdId,
    String seatId,
    int ttlSeconds,
    LocalDateTime expiresAt
) {}
