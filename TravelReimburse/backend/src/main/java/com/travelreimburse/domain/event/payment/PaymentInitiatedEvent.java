package com.travelreimburse.domain.event.payment;

import java.time.LocalDateTime;

/**
 * Domain Event - wird publishet wenn Payment zu EasyPay submitted wurde
 * Triggert asynchrone Payment-Verarbeitung
 */
public record PaymentInitiatedEvent(
    Long paymentId,
    Long travelRequestId,
    String easyPayTransactionId,
    LocalDateTime timestamp
) {}

