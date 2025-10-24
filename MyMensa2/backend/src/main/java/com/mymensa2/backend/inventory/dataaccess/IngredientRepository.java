package com.mymensa2.backend.inventory.dataaccess;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Integer> {
    
    // Findet alle Zutaten mit niedrigem Bestand
    @Query("SELECT i FROM Ingredient i WHERE i.stockQuantity < i.minStockLevel")
    List<Ingredient> findLowStock();
}
