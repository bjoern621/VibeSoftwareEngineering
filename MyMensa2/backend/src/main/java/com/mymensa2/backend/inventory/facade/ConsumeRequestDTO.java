package com.mymensa2.backend.inventory.facade;

public record ConsumeRequestDTO(
    Integer mealId,
    Integer quantity
) {}
