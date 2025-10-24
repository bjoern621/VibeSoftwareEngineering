package com.mymensa2.backend.inventory.facade;

public record IngredientResponseDTO(
    Integer id,
    String name,
    String unit,
    Float stockQuantity,
    Float minStockLevel,
    Float pricePerUnit,
    String supplierId,
    Boolean needsReorder
) {}
