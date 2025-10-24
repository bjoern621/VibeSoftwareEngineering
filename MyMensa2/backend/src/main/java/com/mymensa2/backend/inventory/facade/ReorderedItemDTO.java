package com.mymensa2.backend.inventory.facade;

public record ReorderedItemDTO(
    Integer ingredientId,
    String ingredientName,
    Float currentStock,
    Float minStockLevel,
    Float reorderQuantity,
    String supplierId,
    String foodsupplyOrderId
) {}
