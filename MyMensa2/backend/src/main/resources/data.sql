-- ========================================
-- MyMensa2 Testdaten
-- ========================================

-- Gerichte (Meals)
INSERT INTO meals (name, description, price, cost, ingredients, calories, protein, carbs, fat, deleted, deleted_at) VALUES
('Spaghetti Bolognese', 'Italienische Pasta mit Hackfleischsauce', 6.5, 3.2, 'Nudeln, Hackfleisch, Tomatensauce, Zwiebeln', 650, 28.5, 75.0, 18.3, false, null),
('Veganer Burger', 'Burger mit pflanzlichem Patty und Gemüse', 7.9, 4.5, 'Veganes Patty, Brötchen, Salat, Tomaten, Zwiebeln', 520, 22.0, 58.0, 16.0, false, null),
('Caesar Salad', 'Römersalat mit Hähnchen und Parmesan', 6.2, 3.0, 'Salat, Hähnchen, Parmesan, Croutons, Caesar-Dressing', 380, 32.0, 18.0, 20.0, false, null),
('Currywurst mit Pommes', 'Klassische deutsche Currywurst', 5.5, 2.8, 'Bratwurst, Currysauce, Pommes Frites', 720, 18.0, 68.0, 38.0, false, null),
('Gemüse-Lasagne', 'Vegetarische Lasagne mit frischem Gemüse', 7.2, 3.5, 'Lasagneplatten, Zucchini, Aubergine, Tomatensauce, Mozzarella', 580, 24.0, 62.0, 22.0, false, null);

-- Kategorien für Gerichte
INSERT INTO meal_categories (meal_id, category) VALUES
(2, 'Vegan'),
(2, 'Vegetarisch'),
(3, 'Glutenfrei'),
(5, 'Vegetarisch');

-- Allergene für Gerichte
INSERT INTO meal_allergens (meal_id, allergen) VALUES
(1, 'Gluten'),
(1, 'Milch/Laktose'),
(3, 'Milch/Laktose'),
(3, 'Eier'),
(4, 'Senf'),
(5, 'Gluten'),
(5, 'Milch/Laktose');

-- Speisepläne (Meal Plans)
INSERT INTO meal_plans (meal_id, date, stock) VALUES
(1, '2025-01-13', 50),
(2, '2025-01-13', 30),
(3, '2025-01-13', 25),
(1, '2025-01-14', 45),
(4, '2025-01-14', 40),
(5, '2025-01-15', 35),
(2, '2025-01-15', 28),
(3, '2025-01-16', 22),
(1, '2025-01-16', 48),
(4, '2025-01-17', 38);

-- Bestellungen (Orders)
INSERT INTO orders (meal_id, order_date, pickup_date, paid, paid_at, payment_method, payment_transaction_id, qr_code, collected, collected_at) VALUES
(1, '2025-01-12 14:30:00', '2025-01-13', true, '2025-01-12 14:35:00', 'CREDIT_CARD', 'EASYPAY-TXN-789456', 'ORDER-1', true, '2025-01-13 12:15:00'),
(2, '2025-01-12 15:00:00', '2025-01-13', true, '2025-01-12 15:05:00', 'DEBIT_CARD', 'EASYPAY-TXN-789457', 'ORDER-2', true, '2025-01-13 12:30:00'),
(3, '2025-01-12 16:00:00', '2025-01-13', true, '2025-01-12 16:10:00', 'PREPAID_ACCOUNT', 'EASYPAY-TXN-789458', 'ORDER-3', false, null),
(1, '2025-01-13 09:00:00', '2025-01-14', true, '2025-01-13 09:05:00', 'BITCOIN', 'EASYPAY-TXN-789459', 'ORDER-4', false, null),
(4, '2025-01-13 10:30:00', '2025-01-14', false, null, null, null, null, false, null);

-- Zutaten (Ingredients)
INSERT INTO ingredients (name, unit, stock_quantity, min_stock_level, price_per_unit, supplier_id) VALUES
('Tomaten', 'kg', 50.0, 10.0, 2.5, 'FOODSUPPLY-VENDOR-123'),
('Nudeln', 'kg', 80.0, 20.0, 1.8, 'FOODSUPPLY-VENDOR-456'),
('Hackfleisch', 'kg', 35.0, 15.0, 8.5, 'FOODSUPPLY-VENDOR-789'),
('Salat', 'Stück', 45.0, 10.0, 1.2, 'FOODSUPPLY-VENDOR-123'),
('Kartoffeln', 'kg', 120.0, 30.0, 1.5, 'FOODSUPPLY-VENDOR-456'),
('Mozzarella', 'kg', 8.5, 10.0, 9.2, 'FOODSUPPLY-VENDOR-789'),
('Veganes Patty', 'Stück', 60.0, 20.0, 2.8, 'FOODSUPPLY-VENDOR-123'),
('Bratwurst', 'Stück', 75.0, 25.0, 1.9, 'FOODSUPPLY-VENDOR-789');

-- Personal (Staff)
INSERT INTO staff (first_name, last_name, role, staffman_id, is_available) VALUES
('Anna', 'Schmidt', 'COOK', 'STAFFMAN-EMP-001', true),
('Max', 'Müller', 'COOK', 'STAFFMAN-EMP-002', true),
('Sarah', 'Weber', 'SERVICE', 'STAFFMAN-EMP-003', true),
('Tom', 'Fischer', 'SERVICE', 'STAFFMAN-EMP-004', true),
('Lisa', 'Wagner', 'MANAGER', 'STAFFMAN-EMP-005', true),
('Peter', 'Becker', 'COOK', 'STAFFMAN-EMP-006', false);

-- Arbeitszeiten (Working Hours)
INSERT INTO working_hours (staff_id, date, start_time, end_time, hours_worked, synced_to_staffman) VALUES
(1, '2025-01-13', '08:00:00', '16:00:00', 8.0, true),
(2, '2025-01-13', '08:00:00', '16:00:00', 8.0, true),
(3, '2025-01-13', '10:00:00', '18:00:00', 8.0, true),
(4, '2025-01-13', '10:00:00', '18:00:00', 8.0, true),
(1, '2025-01-14', '08:00:00', '16:00:00', 8.0, true),
(2, '2025-01-14', '08:00:00', '14:00:00', 6.0, true),
(3, '2025-01-14', '10:00:00', '18:00:00', 8.0, true),
(5, '2025-01-14', '09:00:00', '17:00:00', 8.0, true);
