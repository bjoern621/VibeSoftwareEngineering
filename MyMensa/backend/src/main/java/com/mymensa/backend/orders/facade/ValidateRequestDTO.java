package com.mymensa.backend.orders.facade;

/**
 * DTO für QR-Code Validierung Request
 */
public record ValidateRequestDTO(
    String qrCode
) {}
