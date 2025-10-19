package com.mymensa.backend.orders.logic;

import com.mymensa.backend.common.InvalidRequestException;
import com.mymensa.backend.common.ResourceNotFoundException;
import com.mymensa.backend.meals.dataaccess.Meal;
import com.mymensa.backend.meals.dataaccess.MealRepository;
import com.mymensa.backend.meals.facade.MealDTO;
import com.mymensa.backend.meals.logic.MealService;
import com.mymensa.backend.mealplans.dataaccess.MealPlan;
import com.mymensa.backend.mealplans.dataaccess.MealPlanRepository;
import com.mymensa.backend.orders.dataaccess.Order;
import com.mymensa.backend.orders.dataaccess.OrderRepository;
import com.mymensa.backend.orders.facade.OrderResponseDTO;
import com.mymensa.backend.orders.facade.PaymentResponseDTO;
import com.mymensa.backend.orders.facade.ValidateResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private MealRepository mealRepository;
    
    @Autowired
    private MealPlanRepository mealPlanRepository;
    
    @Autowired
    private MealService mealService;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * Erstellt eine neue Bestellung
     */
    @Transactional
    public OrderResponseDTO createOrder(Integer mealId, String dateString) {
        // Validierung
        if (mealId == null) {
            throw new InvalidRequestException("mealId ist erforderlich");
        }
        if (dateString == null || dateString.isEmpty()) {
            throw new InvalidRequestException("date ist erforderlich");
        }
        
        // Datum parsen
        LocalDate date = parseDate(dateString);
        
        // Prüfen ob Meal existiert UND nicht gelöscht ist
        Meal meal = mealRepository.findById(mealId)
            .orElseThrow(() -> new ResourceNotFoundException("Gericht mit ID " + mealId + " nicht gefunden"));
        
        if (meal.getDeleted()) {
            throw new InvalidRequestException("Dieses Gericht ist nicht mehr verfügbar");
        }
        
        // Prüfen ob Meal für diesen Tag im Speiseplan ist und Bestand verfügbar
        MealPlan mealPlan = mealPlanRepository.findByMealIdAndDate(mealId, date)
            .orElseThrow(() -> new InvalidRequestException("Gericht ist für diesen Tag nicht im Speiseplan verfügbar"));
        
        if (mealPlan.getStock() <= 0) {
            throw new InvalidRequestException("Nicht genügend Bestand verfügbar");
        }
        
        // Bestand reduzieren
        mealPlan.setStock(mealPlan.getStock() - 1);
        mealPlanRepository.save(mealPlan);
        
        // Bestellung erstellen
        Order order = new Order(meal, date);
        Order savedOrder = orderRepository.save(order);
        
        return new OrderResponseDTO(savedOrder.getId());
    }
    
    /**
     * Bezahlt eine Bestellung und generiert QR-Code
     */
    @Transactional
    public PaymentResponseDTO payOrder(Integer orderId) {
        if (orderId == null) {
            throw new InvalidRequestException("orderId ist erforderlich");
        }
        
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Bestellung mit ID " + orderId + " nicht gefunden"));
        
        if (order.getPaid()) {
            throw new InvalidRequestException("Bestellung wurde bereits bezahlt");
        }
        
        // Als bezahlt markieren und QR-Code generieren
        order.setPaid(true);
        String qrCode = "ORDER-" + orderId;
        order.setQrCode(qrCode);
        
        orderRepository.save(order);
        
        return new PaymentResponseDTO(qrCode);
    }
    
    /**
     * Validiert einen QR-Code und markiert Bestellung als abgeholt
     */
    @Transactional
    public ValidateResponseDTO validateQrCode(String qrCode) {
        if (qrCode == null || qrCode.isEmpty()) {
            throw new InvalidRequestException("qrCode ist erforderlich");
        }
        
        Order order = orderRepository.findByQrCode(qrCode)
            .orElseThrow(() -> new ResourceNotFoundException("QR-Code ungültig"));
        
        boolean wasAlreadyCollected = order.getCollected();
        
        // Wenn noch nicht abgeholt, jetzt markieren
        if (!wasAlreadyCollected) {
            order.setCollected(true);
            order.setCollectedAt(LocalDateTime.now());
            orderRepository.save(order);
        }
        
        // MealDTO erstellen (auch gelöschte Meals für historische Orders)
        MealDTO mealDTO = mealService.getMealByIdIncludingDeleted(order.getMeal().getId());
        
        return new ValidateResponseDTO(
            wasAlreadyCollected,
            order.getCollectedAt() != null ? order.getCollectedAt().toString() : null,
            order.getId(),
            order.getDate().format(DATE_FORMATTER),
            mealDTO
        );
    }
    
    /**
     * Hilfsmethode zum Parsen von Datumsstrings
     */
    private LocalDate parseDate(String dateString) {
        try {
            return LocalDate.parse(dateString, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new InvalidRequestException("Ungültiges Datumsformat. Erwartet: YYYY-MM-DD");
        }
    }
}
