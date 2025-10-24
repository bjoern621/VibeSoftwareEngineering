package com.mymensa2.backend.orders.facade;

public record PaymentRequestDTO(
    String paymentMethod,
    String paymentTransactionId
) {}
