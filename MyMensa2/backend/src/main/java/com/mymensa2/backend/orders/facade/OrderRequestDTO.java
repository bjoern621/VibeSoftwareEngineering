package com.mymensa2.backend.orders.facade;

/**
 * DTO für die Erstellung einer Bestellung
 */
public record OrderRequestDTO(
    Integer mealId,
    String date  // Format: YYYY-MM-DD
) {}
