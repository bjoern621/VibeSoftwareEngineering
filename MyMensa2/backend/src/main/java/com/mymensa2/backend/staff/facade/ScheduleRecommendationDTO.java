package com.mymensa2.backend.staff.facade;

public record ScheduleRecommendationDTO(
    String date,
    Integer expectedVisitors,
    Integer plannedMeals,
    RecommendedStaffDTO recommendedStaff,
    StaffByRoleDTO availableStaff
) {}
