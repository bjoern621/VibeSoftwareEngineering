package com.travelreimburse.presentation.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO für EasyPay Callback (Request)
 * Das ist was EasyPay-System sendet, wenn ein Payment Status sich ändert
 */
public record EasyPayCallbackDTO(
    @NotBlank(message = "Transaction ID darf nicht leer sein")
    String transactionId,

    @NotBlank(message = "Status darf nicht leer sein")
    String status,

    String reason
) {}

