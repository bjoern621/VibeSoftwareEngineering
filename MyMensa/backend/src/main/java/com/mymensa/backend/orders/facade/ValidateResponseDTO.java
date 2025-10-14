package com.mymensa.backend.orders.facade;

import com.mymensa.backend.meals.facade.MealDTO;

/**
 * DTO für QR-Code Validierung Response
 */
public record ValidateResponseDTO(
    Boolean alreadyCollected,
    String collectedAt,  // ISO-8601 format or null
    Integer orderId,
    String orderDate,    // Format: YYYY-MM-DD
    MealDTO meal
) {}
