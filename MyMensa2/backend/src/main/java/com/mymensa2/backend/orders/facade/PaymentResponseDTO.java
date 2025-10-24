package com.mymensa2.backend.orders.facade;

import java.time.LocalDateTime;

public record PaymentResponseDTO(
    Integer orderId,
    String qrCode,
    LocalDateTime paidAt,
    String paymentMethod,
    String paymentTransactionId
) {}
