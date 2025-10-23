package com.mymensa2.backend.mealplans.logic;

import com.mymensa2.backend.mealplans.facade.MealPlanResponseDTO;

/**
 * Wrapper-Klasse für MealPlan-Response mit Status-Information
 */
public class MealPlanResult {
    private final MealPlanResponseDTO response;
    private final boolean created;
    
    public MealPlanResult(MealPlanResponseDTO response, boolean created) {
        this.response = response;
        this.created = created;
    }
    
    public MealPlanResponseDTO getResponse() {
        return response;
    }
    
    public boolean isCreated() {
        return created;
    }
}
