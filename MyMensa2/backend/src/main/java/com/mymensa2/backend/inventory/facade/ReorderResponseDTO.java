package com.mymensa2.backend.inventory.facade;

import java.util.List;

public record ReorderResponseDTO(
    List<ReorderedItemDTO> reorderedItems,
    Double totalOrderValue
) {}
