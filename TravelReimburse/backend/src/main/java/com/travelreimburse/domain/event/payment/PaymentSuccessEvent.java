package com.travelreimburse.domain.event.payment;

import java.time.LocalDateTime;

/**
 * Domain Event - wird publishet wenn Payment erfolgreich ist
 */
public record PaymentSuccessEvent(
    Long paymentId,
    Long travelRequestId,
    String easyPayTransactionId,
    LocalDateTime timestamp
) {}

