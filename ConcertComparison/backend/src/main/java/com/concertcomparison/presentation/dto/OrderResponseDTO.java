package com.concertcomparison.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO für Order-Informationen.
 * 
 * <p>Wird zurückgegeben nach erfolgreichem Kauf oder bei Order-Abfragen.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {

    /**
     * ID der Order.
     */
    private String orderId;

    /**
     * ID des gekauften Seats.
     */
    private String seatId;

    /**
     * ID des Käufers.
     */
    private String userId;

    /**
     * Seat-Nummer (z.B. "A-1", "VIP-001").
     */
    private String seatNumber;

    /**
     * Kategorie des Seats (z.B. "VIP", "CATEGORY_A").
     */
    private String category;

    /**
     * Gesamtpreis des Tickets (in EUR).
     */
    private Double totalPrice;

    /**
     * Status der Order (z.B. "CONFIRMED").
     */
    private String status;

    /**
     * Zeitstempel der Order-Erstellung.
     */
    private LocalDateTime createdAt;
}
