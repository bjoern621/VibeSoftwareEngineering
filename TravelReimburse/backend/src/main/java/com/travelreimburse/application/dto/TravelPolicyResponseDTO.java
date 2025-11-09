package com.travelreimburse.application.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO für die Response einer TravelPolicy
 */
public record TravelPolicyResponseDTO(
    Long id,
    String name,
    String description,
    String departmentCode,
    String location,
    Map<String, String> categoryLimits,  // Category name -> max amount
    boolean autoApprovalEnabled,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
