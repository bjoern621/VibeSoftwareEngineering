package com.mymensa.backend.orders.facade;

import com.mymensa.backend.orders.logic.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:3000")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    /**
     * POST /api/orders - Bestellung erstellen
     * 
     * Request Body: { "mealId": 1, "date": "2025-01-13" }
     * Response: 201 Created mit { "orderId": 1 }
     */
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody OrderRequestDTO request) {
        OrderResponseDTO response = orderService.createOrder(request.mealId(), request.date());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * PUT /api/orders/{orderId}/pay - Bestellung bezahlen und QR-Code generieren
     * 
     * Response: 200 OK mit { "qrCode": "ORDER-1" }
     */
    @PutMapping("/{orderId}/pay")
    public ResponseEntity<PaymentResponseDTO> payOrder(@PathVariable Integer orderId) {
        PaymentResponseDTO response = orderService.payOrder(orderId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/orders/validate - QR-Code validieren und als abgeholt markieren
     * 
     * Request Body: { "qrCode": "ORDER-1" }
     * Response: 200 OK mit Bestellungsdetails
     */
    @PostMapping("/validate")
    public ResponseEntity<ValidateResponseDTO> validateQrCode(@RequestBody ValidateRequestDTO request) {
        ValidateResponseDTO response = orderService.validateQrCode(request.qrCode());
        return ResponseEntity.ok(response);
    }
}
