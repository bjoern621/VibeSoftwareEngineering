package com.travelreimburse.domain.event.payment;

import java.time.LocalDateTime;

/**
 * Domain Event - wird publishet wenn Payment fehlschlägt
 */
public record PaymentFailedEvent(
    Long paymentId,
    Long travelRequestId,
    String reason,
    LocalDateTime timestamp
) {}

