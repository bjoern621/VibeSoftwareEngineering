package com.concertcomparison.presentation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO für Ticket-Kauf (Purchase).
 * 
 * <p>Verwendet bei POST /api/orders zum Abschluss eines Kaufs
 * basierend auf einer aktiven Reservation.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequestDTO {

    /**
     * ID der Reservation (Hold), die gekauft werden soll.
     */
    @NotNull(message = "ReservationId darf nicht null sein")
    private String reservationId;

    /**
     * ID des Käufers (muss mit Reservation-Owner übereinstimmen).
     */
    @NotNull(message = "UserId darf nicht null sein")
    private String userId;
}
