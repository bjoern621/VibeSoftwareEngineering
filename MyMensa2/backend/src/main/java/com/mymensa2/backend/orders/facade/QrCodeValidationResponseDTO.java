package com.mymensa2.backend.orders.facade;

import com.mymensa2.backend.meals.facade.MealResponseDTO;

import java.time.LocalDateTime;

public record QrCodeValidationResponseDTO(
    Boolean alreadyCollected,
    LocalDateTime collectedAt,
    Integer orderId,
    LocalDateTime orderDate,
    String pickupDate,
    MealResponseDTO meal
) {}
