package com.mymensa2.backend.inventory.facade;

public record ConsumedIngredientDTO(
    String ingredientName,
    Float quantityUsed,
    String unit,
    Float remainingStock,
    Boolean needsReorder
) {}
