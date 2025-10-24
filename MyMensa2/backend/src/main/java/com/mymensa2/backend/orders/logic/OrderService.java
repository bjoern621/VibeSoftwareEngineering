package com.mymensa2.backend.orders.logic;

import com.mymensa2.backend.common.InvalidRequestException;
import com.mymensa2.backend.common.ResourceNotFoundException;
import com.mymensa2.backend.meals.dataaccess.Meal;
import com.mymensa2.backend.meals.facade.MealResponseDTO;
import com.mymensa2.backend.meals.logic.MealService;
import com.mymensa2.backend.mealplans.logic.MealPlanService;
import com.mymensa2.backend.orders.dataaccess.Order;
import com.mymensa2.backend.orders.dataaccess.OrderRepository;
import com.mymensa2.backend.orders.facade.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final MealPlanService mealPlanService;
    private final MealService mealService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public OrderService(OrderRepository orderRepository, MealPlanService mealPlanService, MealService mealService) {
        this.orderRepository = orderRepository;
        this.mealPlanService = mealPlanService;
        this.mealService = mealService;
    }
    
    // Bestellung erstellen
    @Transactional
    public OrderCreateResponseDTO createOrder(OrderCreateRequestDTO request) {
        validateOrderRequest(request);
        
        Integer mealId = request.mealId();
        LocalDate pickupDate = parseDate(request.pickupDate());
        
        // Prüfe, ob Gericht im Speiseplan verfügbar ist
        if (!mealPlanService.isAvailable(mealId, pickupDate)) {
            throw new InvalidRequestException("Gericht ist nicht verfügbar oder nicht im Speiseplan für diesen Tag");
        }
        
        // Reduziere Bestand
        mealPlanService.decreaseStock(mealId, pickupDate);
        
        // Erstelle Bestellung
        Order order = new Order(mealId, pickupDate);
        Order savedOrder = orderRepository.save(order);
        
        return new OrderCreateResponseDTO(
                savedOrder.getId(),
                savedOrder.getOrderDate(),
                savedOrder.getPickupDate().format(DATE_FORMATTER),
                savedOrder.getPaid(),
                savedOrder.getCollected()
        );
    }
    
    // Bestellung bezahlen
    @Transactional
    public PaymentResponseDTO payOrder(Integer orderId, PaymentRequestDTO request) {
        validatePaymentRequest(request);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Bestellung mit ID " + orderId + " nicht gefunden"));
        
        if (order.getPaid()) {
            throw new InvalidRequestException("Bestellung wurde bereits bezahlt");
        }
        
        // Setze Zahlungsinformationen
        order.setPaid(true);
        order.setPaidAt(LocalDateTime.now());
        order.setPaymentMethod(request.paymentMethod());
        order.setPaymentTransactionId(request.paymentTransactionId());
        
        // Generiere QR-Code
        String qrCode = "ORDER-" + orderId;
        order.setQrCode(qrCode);
        
        Order updatedOrder = orderRepository.save(order);
        
        return new PaymentResponseDTO(
                updatedOrder.getId(),
                updatedOrder.getQrCode(),
                updatedOrder.getPaidAt(),
                updatedOrder.getPaymentMethod(),
                updatedOrder.getPaymentTransactionId()
        );
    }
    
    // QR-Code validieren
    @Transactional
    public QrCodeValidationResponseDTO validateQrCode(QrCodeValidationRequestDTO request) {
        if (request.qrCode() == null || request.qrCode().trim().isEmpty()) {
            throw new InvalidRequestException("QR-Code ist erforderlich");
        }
        
        Order order = orderRepository.findByQrCode(request.qrCode())
                .orElseThrow(() -> new ResourceNotFoundException("QR-Code ungültig oder Bestellung nicht bezahlt"));
        
        boolean alreadyCollected = order.getCollected();
        
        // Markiere als abgeholt, wenn noch nicht geschehen
        if (!alreadyCollected) {
            order.setCollected(true);
            order.setCollectedAt(LocalDateTime.now());
            orderRepository.save(order);
        }
        
        Meal meal = mealService.getMealByIdIncludingDeleted(order.getMealId());
        
        return new QrCodeValidationResponseDTO(
                alreadyCollected,
                order.getCollectedAt(),
                order.getId(),
                order.getOrderDate(),
                order.getPickupDate().format(DATE_FORMATTER),
                convertMealToDTO(meal)
        );
    }
    
    // Alle Bestellungen abrufen (mit Filtern)
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getAllOrders(String startDateStr, String endDateStr, Boolean paid, Boolean collected) {
        List<Order> orders;
        
        if (startDateStr != null && endDateStr != null) {
            LocalDate startDate = parseDate(startDateStr);
            LocalDate endDate = parseDate(endDateStr);
            orders = orderRepository.findByPickupDateBetween(startDate, endDate);
        } else {
            orders = orderRepository.findAll();
        }
        
        // Filtere nach Zahlungsstatus
        if (paid != null) {
            orders = orders.stream()
                    .filter(o -> o.getPaid().equals(paid))
                    .collect(Collectors.toList());
        }
        
        // Filtere nach Abholstatus
        if (collected != null) {
            orders = orders.stream()
                    .filter(o -> o.getCollected().equals(collected))
                    .collect(Collectors.toList());
        }
        
        return orders.stream()
                .map(this::convertToOrderResponseDTO)
                .collect(Collectors.toList());
    }
    
    // Hilfsmethode: Datum parsen
    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new InvalidRequestException("Ungültiges Datumsformat. Erwartet: yyyy-MM-dd");
        }
    }
    
    // Validierung der Order-Request-Daten
    private void validateOrderRequest(OrderCreateRequestDTO request) {
        if (request.mealId() == null) {
            throw new InvalidRequestException("Gericht-ID ist erforderlich");
        }
        if (request.pickupDate() == null || request.pickupDate().trim().isEmpty()) {
            throw new InvalidRequestException("Abholdatum ist erforderlich");
        }
    }
    
    // Validierung der Payment-Request-Daten
    private void validatePaymentRequest(PaymentRequestDTO request) {
        if (request.paymentMethod() == null || request.paymentMethod().trim().isEmpty()) {
            throw new InvalidRequestException("Zahlungsmethode ist erforderlich");
        }
        
        List<String> validMethods = List.of("CREDIT_CARD", "DEBIT_CARD", "PREPAID_ACCOUNT", "BITCOIN");
        if (!validMethods.contains(request.paymentMethod())) {
            throw new InvalidRequestException("Ungültige Zahlungsmethode. Erlaubt: " + String.join(", ", validMethods));
        }
        
        if (request.paymentTransactionId() == null || request.paymentTransactionId().trim().isEmpty()) {
            throw new InvalidRequestException("Transaktions-ID ist erforderlich");
        }
    }
    
    // Konvertierung von Meal-Entity zu DTO
    private MealResponseDTO convertMealToDTO(Meal meal) {
        return new MealResponseDTO(
                meal.getId(),
                meal.getName(),
                meal.getDescription(),
                meal.getPrice(),
                meal.getCost(),
                meal.getIngredients(),
                meal.getNutritionalInfo(),
                meal.getCategories(),
                meal.getAllergens(),
                meal.getDeleted(),
                meal.getDeletedAt()
        );
    }
    
    // Konvertierung von Order-Entity zu OrderResponseDTO
    private OrderResponseDTO convertToOrderResponseDTO(Order order) {
        Meal meal = mealService.getMealByIdIncludingDeleted(order.getMealId());
        
        return new OrderResponseDTO(
                order.getId(),
                convertMealToDTO(meal),
                order.getOrderDate(),
                order.getPickupDate().format(DATE_FORMATTER),
                order.getPaid(),
                order.getPaidAt(),
                order.getPaymentMethod(),
                order.getCollected(),
                order.getCollectedAt()
        );
    }
}
