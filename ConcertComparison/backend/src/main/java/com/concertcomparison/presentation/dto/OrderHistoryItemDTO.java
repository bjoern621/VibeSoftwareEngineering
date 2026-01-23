package com.concertcomparison.presentation.dto;

import com.concertcomparison.domain.model.OrderStatus;

import java.time.LocalDateTime;

/**
 * Response DTO für Order History (US-179).
 * 
 * Enthält angereicherte Informationen über Concert und Seat für bessere UX.
 * 
 * @author CONCERT COMPARISON Team
 * @since US-179
 */
public class OrderHistoryItemDTO {

    private Long orderId;
    private OrderStatus status;
    private Double totalPrice;
    private LocalDateTime purchaseDate;
    private String paymentStatus;
    
    // Concert Details
    private Long concertId;
    private String concertName;
    private String venue;
    private LocalDateTime concertDate;
    
    // Seat Details
    private Long seatId;
    private String seatNumber;
    private String category;
    private String block;
    private String row;
    private String number;

    // ==================== CONSTRUCTORS ====================

    public OrderHistoryItemDTO() {
        // Default Constructor für Jackson
    }

    /**
     * Vollständiger Constructor für manuelle Mapping.
     */
    public OrderHistoryItemDTO(
            Long orderId,
            OrderStatus status,
            Double totalPrice,
            LocalDateTime purchaseDate,
            String paymentStatus,
            Long concertId,
            String concertName,
            String venue,
            LocalDateTime concertDate,
            Long seatId,
            String seatNumber,
            String category,
            String block,
            String row,
            String number) {
        this.orderId = orderId;
        this.status = status;
        this.totalPrice = totalPrice;
        this.purchaseDate = purchaseDate;
        this.paymentStatus = paymentStatus;
        this.concertId = concertId;
        this.concertName = concertName;
        this.venue = venue;
        this.concertDate = concertDate;
        this.seatId = seatId;
        this.seatNumber = seatNumber;
        this.category = category;
        this.block = block;
        this.row = row;
        this.number = number;
    }

    // ==================== GETTERS & SETTERS ====================

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Long getConcertId() {
        return concertId;
    }

    public void setConcertId(Long concertId) {
        this.concertId = concertId;
    }

    public String getConcertName() {
        return concertName;
    }

    public void setConcertName(String concertName) {
        this.concertName = concertName;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public LocalDateTime getConcertDate() {
        return concertDate;
    }

    public void setConcertDate(LocalDateTime concertDate) {
        this.concertDate = concertDate;
    }

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public String getRow() {
        return row;
    }

    public void setRow(String row) {
        this.row = row;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
