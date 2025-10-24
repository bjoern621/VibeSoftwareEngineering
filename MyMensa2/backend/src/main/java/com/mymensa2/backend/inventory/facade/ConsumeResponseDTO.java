package com.mymensa2.backend.inventory.facade;

import java.util.List;

public record ConsumeResponseDTO(
    String mealName,
    Integer quantityPrepared,
    List<ConsumedIngredientDTO> ingredientsConsumed
) {}
