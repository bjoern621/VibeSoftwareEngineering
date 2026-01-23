package com.concertcomparison.presentation.dto;

/**
 * DTO für QR Code Ticket Content (US-179).
 * 
 * Struktur der Daten die im QR Code encodiert werden.
 * Format: JSON String für bessere Erweiterbarkeit.
 * 
 * @author CONCERT COMPARISON Team
 * @since US-179
 */
public class TicketDTO {

    private Long orderId;
    private Long concertId;
    private Long seatId;
    private String userId;

    // ==================== CONSTRUCTORS ====================

    public TicketDTO() {
        // Default Constructor für Jackson
    }

    public TicketDTO(Long orderId, Long concertId, Long seatId, String userId) {
        this.orderId = orderId;
        this.concertId = concertId;
        this.seatId = seatId;
        this.userId = userId;
    }

    // ==================== GETTERS & SETTERS ====================

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getConcertId() {
        return concertId;
    }

    public void setConcertId(Long concertId) {
        this.concertId = concertId;
    }

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Konvertiert Ticket in einen String für QR Code.
     * 
     * Format: orderId|concertId|seatId|userId
     * 
     * @return QR Code Content als String
     */
    public String toQrCodeContent() {
        return String.format("%d|%d|%d|%s", orderId, concertId, seatId, userId);
    }

    /**
     * Erstellt TicketDTO aus QR Code String.
     * 
     * @param qrCodeContent Format: orderId|concertId|seatId|userId
     * @return TicketDTO
     * @throws IllegalArgumentException bei ungültigem Format
     */
    public static TicketDTO fromQrCodeContent(String qrCodeContent) {
        String[] parts = qrCodeContent.split("\\|");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Ungültiges QR Code Format: " + qrCodeContent);
        }
        
        try {
            return new TicketDTO(
                Long.parseLong(parts[0]),
                Long.parseLong(parts[1]),
                Long.parseLong(parts[2]),
                parts[3]
            );
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ungültige Ticket-Daten im QR Code", e);
        }
    }
}
