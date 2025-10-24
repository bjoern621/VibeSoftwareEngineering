package com.mymensa2.backend.orders.facade;

public record OrderCreateRequestDTO(
    Integer mealId,
    String pickupDate
) {}
