package com.mymensa2.backend.inventory.facade;

public record IngredientRequestDTO(
    String name,
    String unit,
    Float stockQuantity,
    Float minStockLevel,
    Float pricePerUnit,
    String supplierId
) {}
