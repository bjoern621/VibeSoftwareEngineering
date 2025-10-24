package com.mymensa2.backend.orders.facade;

import com.mymensa2.backend.meals.facade.MealResponseDTO;

import java.time.LocalDateTime;

public record OrderResponseDTO(
    Integer id,
    MealResponseDTO meal,
    LocalDateTime orderDate,
    String pickupDate,
    Boolean paid,
    LocalDateTime paidAt,
    String paymentMethod,
    Boolean collected,
    LocalDateTime collectedAt
) {}
