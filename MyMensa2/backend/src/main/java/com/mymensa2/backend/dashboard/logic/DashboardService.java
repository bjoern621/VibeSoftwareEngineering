package com.mymensa2.backend.dashboard.logic;

import com.mymensa2.backend.dashboard.facade.DashboardResponseDTO;
import com.mymensa2.backend.dashboard.facade.MealStatDTO;
import com.mymensa2.backend.orders.dataaccess.Order;
import com.mymensa2.backend.orders.dataaccess.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    
    private final OrderRepository orderRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public DashboardService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    
    // Dashboard-Daten abrufen
    @Transactional(readOnly = true)
    public DashboardResponseDTO getDashboardData(String startDateStr, String endDateStr) {
        List<Order> orders;
        
        if (startDateStr != null && endDateStr != null) {
            LocalDate startDate = LocalDate.parse(startDateStr, DATE_FORMATTER);
            LocalDate endDate = LocalDate.parse(endDateStr, DATE_FORMATTER);
            orders = orderRepository.findByPickupDateBetween(startDate, endDate);
        } else {
            orders = orderRepository.findAll();
        }
        
        // Nur bezahlte Bestellungen berücksichtigen
        orders = orders.stream()
                .filter(Order::getPaid)
                .collect(Collectors.toList());
        
        // Gruppiere nach Gericht
        Map<String, List<Order>> groupedByMeal = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getMeal().getName()));
        
        // Berechne Statistiken pro Gericht
        List<MealStatDTO> mealStats = groupedByMeal.entrySet().stream()
                .map(entry -> {
                    String mealName = entry.getKey();
                    List<Order> mealOrders = entry.getValue();
                    
                    int quantitySold = mealOrders.size();
                    double totalRevenue = mealOrders.stream()
                            .mapToDouble(o -> o.getMeal().getPrice().doubleValue())
                            .sum();
                    double totalExpenses = mealOrders.stream()
                            .mapToDouble(o -> o.getMeal().getCost().doubleValue())
                            .sum();
                    
                    return new MealStatDTO(mealName, quantitySold, totalRevenue, totalExpenses);
                })
                .collect(Collectors.toList());
        
        // Berechne Gesamtwerte
        double totalRevenue = mealStats.stream()
                .mapToDouble(MealStatDTO::totalRevenue)
                .sum();
        double totalExpenses = mealStats.stream()
                .mapToDouble(MealStatDTO::totalExpenses)
                .sum();
        double profit = totalRevenue - totalExpenses;
        
        return new DashboardResponseDTO(totalRevenue, totalExpenses, profit, mealStats);
    }
}
