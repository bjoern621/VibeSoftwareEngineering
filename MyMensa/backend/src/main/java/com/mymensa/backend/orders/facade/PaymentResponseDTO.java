package com.mymensa.backend.orders.facade;

/**
 * DTO für die Response nach Bezahlung
 */
public record PaymentResponseDTO(
    String qrCode
) {}
