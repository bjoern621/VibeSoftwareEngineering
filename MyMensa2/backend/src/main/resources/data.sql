-- Testdaten für MyMensa2

-- ============================================
-- GERICHTE (Meals)
-- ============================================

INSERT INTO meals (id, name, description, price, cost, ingredients, calories, protein, carbs, fat, deleted) 
VALUES (1, 'Spaghetti Bolognese', 'Italienische Pasta mit Hackfleischsauce', 6.50, 3.20, 'Nudeln, Hackfleisch, Tomatensauce, Zwiebeln', 650, 28.5, 75.0, 18.3, false);

INSERT INTO meal_categories (meal_id, category) VALUES (1, 'Herzhaft');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (1, 'Gluten');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (1, 'Milch/Laktose');

INSERT INTO meals (id, name, description, price, cost, ingredients, calories, protein, carbs, fat, deleted) 
VALUES (2, 'Veganer Burger', 'Burger mit pflanzlichem Patty', 7.90, 4.50, 'Veganes Patty, Vollkornbrötchen, Salat, Tomate, Gurke', 480, 22.0, 52.0, 15.5, false);

INSERT INTO meal_categories (meal_id, category) VALUES (2, 'Vegan');
INSERT INTO meal_categories (meal_id, category) VALUES (2, 'Vegetarisch');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (2, 'Gluten');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (2, 'Soja');

INSERT INTO meals (id, name, description, price, cost, ingredients, calories, protein, carbs, fat, deleted) 
VALUES (3, 'Hähnchen-Curry', 'Würziges Curry mit Reis', 8.50, 4.80, 'Hähnchenbrust, Reis, Curry-Sauce, Gemüse', 720, 35.0, 68.0, 22.0, false);

INSERT INTO meal_categories (meal_id, category) VALUES (3, 'Halal');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (3, 'Milch/Laktose');

INSERT INTO meals (id, name, description, price, cost, ingredients, calories, protein, carbs, fat, deleted) 
VALUES (4, 'Caesar Salad', 'Frischer Salat mit Parmesan und Croutons', 5.90, 2.80, 'Römersalat, Parmesan, Croutons, Caesar-Dressing', 380, 12.5, 28.0, 24.0, false);

INSERT INTO meal_categories (meal_id, category) VALUES (4, 'Vegetarisch');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (4, 'Gluten');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (4, 'Milch/Laktose');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (4, 'Eier');

INSERT INTO meals (id, name, description, price, cost, ingredients, calories, protein, carbs, fat, deleted) 
VALUES (5, 'Glutenfreie Pizza', 'Pizza mit glutenfreiem Teig', 9.50, 5.20, 'Glutenfreier Teig, Tomaten, Mozzarella, Basilikum', 580, 25.0, 62.0, 20.5, false);

INSERT INTO meal_categories (meal_id, category) VALUES (5, 'Glutenfrei');
INSERT INTO meal_categories (meal_id, category) VALUES (5, 'Vegetarisch');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (5, 'Milch/Laktose');

INSERT INTO meals (id, name, description, price, cost, ingredients, calories, protein, carbs, fat, deleted) 
VALUES (6, 'Tomatensuppe', 'Cremige Tomatensuppe mit frischen Kräutern', 4.50, 2.10, 'Tomaten, Sahne, Zwiebeln, Knoblauch, Basilikum', 280, 6.5, 32.0, 12.0, false);

INSERT INTO meal_categories (meal_id, category) VALUES (6, 'Vegetarisch');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (6, 'Milch/Laktose');

-- ============================================
-- LAGERBESTÄNDE (Ingredients)
-- ============================================

INSERT INTO ingredients (id, name, unit, stock_quantity, min_stock_level, price_per_unit, supplier_id)
VALUES (1, 'Tomaten', 'kg', 50.0, 10.0, 2.50, 'FOODSUPPLY-VENDOR-123');

INSERT INTO ingredients (id, name, unit, stock_quantity, min_stock_level, price_per_unit, supplier_id)
VALUES (2, 'Nudeln', 'kg', 80.0, 20.0, 1.80, 'FOODSUPPLY-VENDOR-456');

INSERT INTO ingredients (id, name, unit, stock_quantity, min_stock_level, price_per_unit, supplier_id)
VALUES (3, 'Hackfleisch', 'kg', 30.0, 15.0, 8.50, 'FOODSUPPLY-VENDOR-789');

INSERT INTO ingredients (id, name, unit, stock_quantity, min_stock_level, price_per_unit, supplier_id)
VALUES (4, 'Salat', 'Stück', 40.0, 10.0, 1.20, 'FOODSUPPLY-VENDOR-123');

INSERT INTO ingredients (id, name, unit, stock_quantity, min_stock_level, price_per_unit, supplier_id)
VALUES (5, 'Reis', 'kg', 100.0, 25.0, 2.20, 'FOODSUPPLY-VENDOR-456');

INSERT INTO ingredients (id, name, unit, stock_quantity, min_stock_level, price_per_unit, supplier_id)
VALUES (6, 'Hähnchenbrust', 'kg', 25.0, 10.0, 9.50, 'FOODSUPPLY-VENDOR-789');

INSERT INTO ingredients (id, name, unit, stock_quantity, min_stock_level, price_per_unit, supplier_id)
VALUES (7, 'Mozzarella', 'kg', 8.0, 12.0, 6.80, 'FOODSUPPLY-VENDOR-123');

INSERT INTO ingredients (id, name, unit, stock_quantity, min_stock_level, price_per_unit, supplier_id)
VALUES (8, 'Veganes Patty', 'Stück', 60.0, 20.0, 2.30, 'FOODSUPPLY-VENDOR-999');

-- ============================================
-- PERSONAL (Staff)
-- ============================================

INSERT INTO staff (id, first_name, last_name, role, staffman_id, is_available)
VALUES (1, 'Anna', 'Schmidt', 'COOK', 'STAFFMAN-EMP-001', true);

INSERT INTO staff (id, first_name, last_name, role, staffman_id, is_available)
VALUES (2, 'Michael', 'Müller', 'COOK', 'STAFFMAN-EMP-002', true);

INSERT INTO staff (id, first_name, last_name, role, staffman_id, is_available)
VALUES (3, 'Laura', 'Weber', 'SERVICE', 'STAFFMAN-EMP-003', true);

INSERT INTO staff (id, first_name, last_name, role, staffman_id, is_available)
VALUES (4, 'Thomas', 'Fischer', 'SERVICE', 'STAFFMAN-EMP-004', true);

INSERT INTO staff (id, first_name, last_name, role, staffman_id, is_available)
VALUES (5, 'Sarah', 'Wagner', 'SERVICE', 'STAFFMAN-EMP-005', false);

INSERT INTO staff (id, first_name, last_name, role, staffman_id, is_available)
VALUES (6, 'David', 'Becker', 'MANAGER', 'STAFFMAN-EMP-006', true);

-- ============================================
-- ARBEITSZEITEN (Working Hours) - Beispielwoche
-- ============================================

INSERT INTO working_hours (id, staff_id, date, start_time, end_time, hours_worked, synced_to_staffman)
VALUES (1, 1, '2025-01-13', '08:00:00', '16:00:00', 8.0, true);

INSERT INTO working_hours (id, staff_id, date, start_time, end_time, hours_worked, synced_to_staffman)
VALUES (2, 2, '2025-01-13', '08:00:00', '16:00:00', 8.0, true);

INSERT INTO working_hours (id, staff_id, date, start_time, end_time, hours_worked, synced_to_staffman)
VALUES (3, 3, '2025-01-13', '10:00:00', '18:00:00', 8.0, true);

INSERT INTO working_hours (id, staff_id, date, start_time, end_time, hours_worked, synced_to_staffman)
VALUES (4, 4, '2025-01-13', '10:00:00', '18:00:00', 8.0, true);

INSERT INTO working_hours (id, staff_id, date, start_time, end_time, hours_worked, synced_to_staffman)
VALUES (5, 1, '2025-01-14', '08:00:00', '16:00:00', 8.0, true);

INSERT INTO working_hours (id, staff_id, date, start_time, end_time, hours_worked, synced_to_staffman)
VALUES (6, 2, '2025-01-14', '08:00:00', '16:00:00', 8.0, true);

INSERT INTO working_hours (id, staff_id, date, start_time, end_time, hours_worked, synced_to_staffman)
VALUES (7, 3, '2025-01-14', '10:00:00', '14:00:00', 4.0, true);

INSERT INTO working_hours (id, staff_id, date, start_time, end_time, hours_worked, synced_to_staffman)
VALUES (8, 4, '2025-01-14', '10:00:00', '18:00:00', 8.0, true);

-- ============================================
-- SPEISEPLÄNE (Meal Plans) - Beispielwoche
-- ============================================

INSERT INTO meal_plans (meal_id, date, stock) VALUES (1, '2025-01-13', 50);
INSERT INTO meal_plans (meal_id, date, stock) VALUES (2, '2025-01-13', 40);
INSERT INTO meal_plans (meal_id, date, stock) VALUES (3, '2025-01-13', 30);

INSERT INTO meal_plans (meal_id, date, stock) VALUES (1, '2025-01-14', 45);
INSERT INTO meal_plans (meal_id, date, stock) VALUES (4, '2025-01-14', 35);
INSERT INTO meal_plans (meal_id, date, stock) VALUES (5, '2025-01-14', 25);

INSERT INTO meal_plans (meal_id, date, stock) VALUES (2, '2025-01-15', 50);
INSERT INTO meal_plans (meal_id, date, stock) VALUES (3, '2025-01-15', 40);
INSERT INTO meal_plans (meal_id, date, stock) VALUES (6, '2025-01-15', 60);

INSERT INTO meal_plans (meal_id, date, stock) VALUES (1, '2025-01-16', 55);
INSERT INTO meal_plans (meal_id, date, stock) VALUES (2, '2025-01-16', 45);
INSERT INTO meal_plans (meal_id, date, stock) VALUES (4, '2025-01-16', 30);

INSERT INTO meal_plans (meal_id, date, stock) VALUES (3, '2025-01-17', 40);
INSERT INTO meal_plans (meal_id, date, stock) VALUES (5, '2025-01-17', 30);
INSERT INTO meal_plans (meal_id, date, stock) VALUES (6, '2025-01-17', 50);
