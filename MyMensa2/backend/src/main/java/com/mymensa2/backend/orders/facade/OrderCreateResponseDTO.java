package com.mymensa2.backend.orders.facade;

import java.time.LocalDateTime;

public record OrderCreateResponseDTO(
    Integer orderId,
    LocalDateTime orderDate,
    String pickupDate,
    Boolean paid,
    Boolean collected
) {}
