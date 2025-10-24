package com.mymensa2.backend.orders.facade;

import com.mymensa2.backend.orders.logic.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:3001")
public class OrderController {
    
    private final OrderService orderService;
    
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    // 8. Bestellung erstellen
    @PostMapping
    public ResponseEntity<OrderCreateResponseDTO> createOrder(@RequestBody OrderCreateRequestDTO request) {
        OrderCreateResponseDTO order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
    
    // 9. Bestellung bezahlen (EASYPAY-Integration)
    @PutMapping("/{orderId}/pay")
    public ResponseEntity<PaymentResponseDTO> payOrder(
            @PathVariable Integer orderId,
            @RequestBody PaymentRequestDTO request) {
        PaymentResponseDTO payment = orderService.payOrder(orderId, request);
        return ResponseEntity.ok(payment);
    }
    
    // 10. Bestellung per QR-Code validieren (Mensa-Mitarbeiter App)
    @PostMapping("/validate")
    public ResponseEntity<QrCodeValidationResponseDTO> validateQrCode(@RequestBody QrCodeValidationRequestDTO request) {
        QrCodeValidationResponseDTO validation = orderService.validateQrCode(request);
        return ResponseEntity.ok(validation);
    }
    
    // 11. Alle Bestellungen abrufen (Admin)
    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) Boolean collected) {
        List<OrderResponseDTO> orders = orderService.getAllOrders(startDate, endDate, paid, collected);
        return ResponseEntity.ok(orders);
    }
}
