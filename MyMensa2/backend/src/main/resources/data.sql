-- ========================================
-- MyMensa2 Testdaten
-- ========================================

-- Gerichte (Meals)
INSERT INTO meals (name, description, price, cost, ingredients, calories, protein, carbs, fat, deleted, deleted_at) VALUES
('Spaghetti Bolognese', 'Italienische Pasta mit Hackfleischsauce', 6.5, 3.2, 'Nudeln, Hackfleisch, Tomatensauce, Zwiebeln', 650, 28.5, 75.0, 18.3, false, null),
('Veganer Burger', 'Burger mit pflanzlichem Patty und Gemüse', 7.9, 4.5, 'Veganes Patty, Brötchen, Salat, Tomaten, Zwiebeln', 520, 22.0, 58.0, 16.0, false, null),
('Caesar Salad', 'Römersalat mit Hähnchen und Parmesan', 6.2, 3.0, 'Salat, Hähnchen, Parmesan, Croutons, Caesar-Dressing', 380, 32.0, 18.0, 20.0, false, null),
('Currywurst mit Pommes', 'Klassische deutsche Currywurst', 5.5, 2.8, 'Bratwurst, Currysauce, Pommes Frites', 720, 18.0, 68.0, 38.0, false, null),
('Gemüse-Lasagne', 'Vegetarische Lasagne mit frischem Gemüse', 7.2, 3.5, 'Lasagneplatten, Zucchini, Aubergine, Tomatensauce, Mozzarella', 580, 24.0, 62.0, 22.0, false, null),
('Gegrillter Lachs mit Reis', 'Frischer Lachs auf Jasminreis mit Gemüse', 9.5, 5.2, 'Lachsfilet, Jasminreis, Brokkoli, Karotten, Zitrone', 520, 38.0, 48.0, 16.5, false, null),
('Hähnchen Shawarma', 'Halal Hähnchen mit orientalischen Gewürzen', 7.8, 3.8, 'Halal Hähnchen, Fladenbrot, Hummus, Tahini, Tomaten, Gurken', 580, 42.0, 52.0, 18.0, false, null),
('Quinoa Bowl', 'Glutenfreie Bowl mit Quinoa und geröstetem Gemüse', 8.2, 4.0, 'Quinoa, Süßkartoffel, Kichererbsen, Avocado, Spinat, Tahini-Dressing', 480, 18.0, 62.0, 16.0, false, null);

-- Kategorien für Gerichte
INSERT INTO meal_categories (meal_id, category) VALUES
(1, 'Fleisch'),
(2, 'Vegan'),
(3, 'Fleisch'),
(4, 'Fleisch'),
(5, 'Vegetarisch'),
(6, 'Fisch'),
(7, 'Halal'),
(7, 'Fleisch'),
(8, 'Glutenfrei'),
(8, 'Vegan'),
(8, 'Vegetarisch');

-- Allergene für Gerichte
INSERT INTO meal_allergens (meal_id, allergen) VALUES
(1, 'Gluten'),
(1, 'Milch/Laktose'),
(3, 'Milch/Laktose'),
(3, 'Eier'),
(4, 'Senf'),
(5, 'Gluten'),
(5, 'Milch/Laktose'),
(6, 'Fisch'),
(7, 'Gluten'),
(7, 'Sesam');

-- Speisepläne (Meal Plans)
INSERT INTO meal_plans (meal_id, date, stock) VALUES
-- Aktuelle Woche (21. - 25. Oktober 2025)
(1, '2025-10-21', 50),
(2, '2025-10-21', 30),
(3, '2025-10-21', 25),
(1, '2025-10-22', 45),
(4, '2025-10-22', 40),
(5, '2025-10-23', 35),
(2, '2025-10-23', 28),
(3, '2025-10-24', 22),
(1, '2025-10-24', 48),
(4, '2025-10-25', 38),
(2, '2025-10-25', 35),
(6, '2025-10-25', 30),
-- Nächste Woche (28. - 31. Oktober 2025)
(1, '2025-10-28', 50),
(2, '2025-10-28', 30),
(7, '2025-10-29', 40),
(3, '2025-10-29', 25),
(4, '2025-10-30', 35),
(5, '2025-10-30', 28),
(6, '2025-10-31', 30),
(8, '2025-10-31', 22),
-- Historische Daten (Januar 2025) für alte Bestellungen
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
-- Alte Bestellungen (Januar 2025)
INSERT INTO orders (meal_id, order_date, pickup_date, paid, paid_at, payment_method, payment_transaction_id, qr_code, collected, collected_at) VALUES
(1, '2025-01-12 14:30:00', '2025-01-13', true, '2025-01-12 14:35:00', 'CREDIT_CARD', 'EASYPAY-TXN-789456', 'ORDER-1', true, '2025-01-13 12:15:00'),
(2, '2025-01-12 15:00:00', '2025-01-13', true, '2025-01-12 15:05:00', 'DEBIT_CARD', 'EASYPAY-TXN-789457', 'ORDER-2', true, '2025-01-13 12:30:00'),
(3, '2025-01-12 16:00:00', '2025-01-13', true, '2025-01-12 16:10:00', 'PREPAID_ACCOUNT', 'EASYPAY-TXN-789458', 'ORDER-3', false, null),
(1, '2025-01-13 09:00:00', '2025-01-14', true, '2025-01-13 09:05:00', 'BITCOIN', 'EASYPAY-TXN-789459', 'ORDER-4', false, null),
(4, '2025-01-13 10:30:00', '2025-01-14', false, null, null, null, null, false, null);

-- Aktuelle Bestellungen für Prognose (letzte 30 Tage: September/Oktober 2025)
-- Spaghetti Bolognese (meal_id=1): ~20 Bestellungen in 30 Tagen
INSERT INTO orders (meal_id, order_date, pickup_date, paid, paid_at, payment_method, payment_transaction_id, qr_code, collected, collected_at) VALUES
(1, '2025-09-25 11:00:00', '2025-09-26', true, '2025-09-25 11:05:00', 'CREDIT_CARD', 'EASYPAY-SEP-001', 'ORDER-SEP-001', true, '2025-09-26 12:00:00'),
(1, '2025-09-26 12:30:00', '2025-09-27', true, '2025-09-26 12:35:00', 'DEBIT_CARD', 'EASYPAY-SEP-002', 'ORDER-SEP-002', true, '2025-09-27 12:15:00'),
(1, '2025-09-28 10:00:00', '2025-09-29', true, '2025-09-28 10:05:00', 'PREPAID_ACCOUNT', 'EASYPAY-SEP-003', 'ORDER-SEP-003', true, '2025-09-29 12:30:00'),
(1, '2025-10-01 11:30:00', '2025-10-02', true, '2025-10-01 11:35:00', 'CREDIT_CARD', 'EASYPAY-OCT-001', 'ORDER-OCT-001', true, '2025-10-02 12:00:00'),
(1, '2025-10-03 13:00:00', '2025-10-04', true, '2025-10-03 13:05:00', 'BITCOIN', 'EASYPAY-OCT-002', 'ORDER-OCT-002', true, '2025-10-04 12:20:00'),
(1, '2025-10-05 12:00:00', '2025-10-06', true, '2025-10-05 12:05:00', 'CREDIT_CARD', 'EASYPAY-OCT-003', 'ORDER-OCT-003', true, '2025-10-06 12:10:00'),
(1, '2025-10-07 11:45:00', '2025-10-08', true, '2025-10-07 11:50:00', 'DEBIT_CARD', 'EASYPAY-OCT-004', 'ORDER-OCT-004', true, '2025-10-08 12:05:00'),
(1, '2025-10-09 14:00:00', '2025-10-10', true, '2025-10-09 14:05:00', 'PREPAID_ACCOUNT', 'EASYPAY-OCT-005', 'ORDER-OCT-005', true, '2025-10-10 12:25:00'),
(1, '2025-10-11 10:30:00', '2025-10-12', true, '2025-10-11 10:35:00', 'CREDIT_CARD', 'EASYPAY-OCT-006', 'ORDER-OCT-006', true, '2025-10-12 12:00:00'),
(1, '2025-10-13 12:15:00', '2025-10-14', true, '2025-10-13 12:20:00', 'BITCOIN', 'EASYPAY-OCT-007', 'ORDER-OCT-007', true, '2025-10-14 12:15:00'),
(1, '2025-10-15 11:00:00', '2025-10-16', true, '2025-10-15 11:05:00', 'CREDIT_CARD', 'EASYPAY-OCT-008', 'ORDER-OCT-008', true, '2025-10-16 12:10:00'),
(1, '2025-10-17 13:30:00', '2025-10-18', true, '2025-10-17 13:35:00', 'DEBIT_CARD', 'EASYPAY-OCT-009', 'ORDER-OCT-009', true, '2025-10-18 12:20:00'),
(1, '2025-10-19 12:00:00', '2025-10-20', true, '2025-10-19 12:05:00', 'PREPAID_ACCOUNT', 'EASYPAY-OCT-010', 'ORDER-OCT-010', true, '2025-10-20 12:00:00'),
(1, '2025-10-21 11:30:00', '2025-10-22', true, '2025-10-21 11:35:00', 'CREDIT_CARD', 'EASYPAY-OCT-011', 'ORDER-OCT-011', true, '2025-10-22 12:15:00'),
(1, '2025-10-23 14:00:00', '2025-10-24', true, '2025-10-23 14:05:00', 'BITCOIN', 'EASYPAY-OCT-012', 'ORDER-OCT-012', true, '2025-10-24 12:10:00'),

-- Veganer Burger (meal_id=2): ~15 Bestellungen in 30 Tagen
(2, '2025-09-26 12:00:00', '2025-09-27', true, '2025-09-26 12:05:00', 'CREDIT_CARD', 'EASYPAY-SEP-101', 'ORDER-SEP-101', true, '2025-09-27 12:30:00'),
(2, '2025-09-29 11:30:00', '2025-09-30', true, '2025-09-29 11:35:00', 'DEBIT_CARD', 'EASYPAY-SEP-102', 'ORDER-SEP-102', true, '2025-09-30 12:15:00'),
(2, '2025-10-02 13:00:00', '2025-10-03', true, '2025-10-02 13:05:00', 'PREPAID_ACCOUNT', 'EASYPAY-OCT-101', 'ORDER-OCT-101', true, '2025-10-03 12:20:00'),
(2, '2025-10-05 12:30:00', '2025-10-06', true, '2025-10-05 12:35:00', 'BITCOIN', 'EASYPAY-OCT-102', 'ORDER-OCT-102', true, '2025-10-06 12:25:00'),
(2, '2025-10-08 11:00:00', '2025-10-09', true, '2025-10-08 11:05:00', 'CREDIT_CARD', 'EASYPAY-OCT-103', 'ORDER-OCT-103', true, '2025-10-09 12:10:00'),
(2, '2025-10-10 14:30:00', '2025-10-11', true, '2025-10-10 14:35:00', 'DEBIT_CARD', 'EASYPAY-OCT-104', 'ORDER-OCT-104', true, '2025-10-11 12:30:00'),
(2, '2025-10-13 12:00:00', '2025-10-14', true, '2025-10-13 12:05:00', 'PREPAID_ACCOUNT', 'EASYPAY-OCT-105', 'ORDER-OCT-105', true, '2025-10-14 12:00:00'),
(2, '2025-10-16 11:30:00', '2025-10-17', true, '2025-10-16 11:35:00', 'CREDIT_CARD', 'EASYPAY-OCT-106', 'ORDER-OCT-106', true, '2025-10-17 12:15:00'),
(2, '2025-10-19 13:00:00', '2025-10-20', true, '2025-10-19 13:05:00', 'BITCOIN', 'EASYPAY-OCT-107', 'ORDER-OCT-107', true, '2025-10-20 12:20:00'),
(2, '2025-10-22 12:30:00', '2025-10-23', true, '2025-10-22 12:35:00', 'CREDIT_CARD', 'EASYPAY-OCT-108', 'ORDER-OCT-108', true, '2025-10-23 12:10:00'),

-- Caesar Salad (meal_id=3): ~12 Bestellungen in 30 Tagen
(3, '2025-09-27 11:00:00', '2025-09-28', true, '2025-09-27 11:05:00', 'DEBIT_CARD', 'EASYPAY-SEP-201', 'ORDER-SEP-201', true, '2025-09-28 12:00:00'),
(3, '2025-10-01 12:30:00', '2025-10-02', true, '2025-10-01 12:35:00', 'CREDIT_CARD', 'EASYPAY-OCT-201', 'ORDER-OCT-201', true, '2025-10-02 12:15:00'),
(3, '2025-10-04 13:00:00', '2025-10-05', true, '2025-10-04 13:05:00', 'PREPAID_ACCOUNT', 'EASYPAY-OCT-202', 'ORDER-OCT-202', true, '2025-10-05 12:20:00'),
(3, '2025-10-07 11:30:00', '2025-10-08', true, '2025-10-07 11:35:00', 'BITCOIN', 'EASYPAY-OCT-203', 'ORDER-OCT-203', true, '2025-10-08 12:25:00'),
(3, '2025-10-10 12:00:00', '2025-10-11', true, '2025-10-10 12:05:00', 'CREDIT_CARD', 'EASYPAY-OCT-204', 'ORDER-OCT-204', true, '2025-10-11 12:10:00'),
(3, '2025-10-14 14:00:00', '2025-10-15', true, '2025-10-14 14:05:00', 'DEBIT_CARD', 'EASYPAY-OCT-205', 'ORDER-OCT-205', true, '2025-10-15 12:30:00'),
(3, '2025-10-17 11:00:00', '2025-10-18', true, '2025-10-17 11:05:00', 'PREPAID_ACCOUNT', 'EASYPAY-OCT-206', 'ORDER-OCT-206', true, '2025-10-18 12:00:00'),
(3, '2025-10-20 13:30:00', '2025-10-21', true, '2025-10-20 13:35:00', 'CREDIT_CARD', 'EASYPAY-OCT-207', 'ORDER-OCT-207', true, '2025-10-21 12:15:00'),
(3, '2025-10-23 12:00:00', '2025-10-24', true, '2025-10-23 12:05:00', 'BITCOIN', 'EASYPAY-OCT-208', 'ORDER-OCT-208', true, '2025-10-24 12:10:00'),

-- Currywurst (meal_id=4): ~10 Bestellungen in 30 Tagen
(4, '2025-09-28 12:00:00', '2025-09-29', true, '2025-09-28 12:05:00', 'CREDIT_CARD', 'EASYPAY-SEP-301', 'ORDER-SEP-301', true, '2025-09-29 12:20:00'),
(4, '2025-10-03 11:30:00', '2025-10-04', true, '2025-10-03 11:35:00', 'DEBIT_CARD', 'EASYPAY-OCT-301', 'ORDER-OCT-301', true, '2025-10-04 12:15:00'),
(4, '2025-10-06 13:00:00', '2025-10-07', true, '2025-10-06 13:05:00', 'PREPAID_ACCOUNT', 'EASYPAY-OCT-302', 'ORDER-OCT-302', true, '2025-10-07 12:25:00'),
(4, '2025-10-09 12:30:00', '2025-10-10', true, '2025-10-09 12:35:00', 'BITCOIN', 'EASYPAY-OCT-303', 'ORDER-OCT-303', true, '2025-10-10 12:10:00'),
(4, '2025-10-12 11:00:00', '2025-10-13', true, '2025-10-12 11:05:00', 'CREDIT_CARD', 'EASYPAY-OCT-304', 'ORDER-OCT-304', true, '2025-10-13 12:00:00'),
(4, '2025-10-16 14:00:00', '2025-10-17', true, '2025-10-16 14:05:00', 'DEBIT_CARD', 'EASYPAY-OCT-305', 'ORDER-OCT-305', true, '2025-10-17 12:20:00'),
(4, '2025-10-19 12:00:00', '2025-10-20', true, '2025-10-19 12:05:00', 'PREPAID_ACCOUNT', 'EASYPAY-OCT-306', 'ORDER-OCT-306', true, '2025-10-20 12:15:00'),
(4, '2025-10-22 13:30:00', '2025-10-23', true, '2025-10-22 13:35:00', 'CREDIT_CARD', 'EASYPAY-OCT-307', 'ORDER-OCT-307', true, '2025-10-23 12:10:00');

-- Zutaten (Ingredients)
INSERT INTO ingredients (name, unit, stock_quantity, min_stock_level, price_per_unit, supplier_id) VALUES
('Tomaten', 'kg', 50.0, 10.0, 2.5, 'FOODSUPPLY-VENDOR-123'),
('Nudeln', 'kg', 80.0, 20.0, 1.8, 'FOODSUPPLY-VENDOR-456'),
('Hackfleisch', 'kg', 35.0, 15.0, 8.5, 'FOODSUPPLY-VENDOR-789'),
('Salat', 'Stück', 45.0, 10.0, 1.2, 'FOODSUPPLY-VENDOR-123'),
('Kartoffeln', 'kg', 120.0, 30.0, 1.5, 'FOODSUPPLY-VENDOR-456'),
('Mozzarella', 'kg', 8.5, 10.0, 9.2, 'FOODSUPPLY-VENDOR-789'),
('Veganes Patty', 'Stück', 60.0, 20.0, 2.8, 'FOODSUPPLY-VENDOR-123'),
('Bratwurst', 'Stück', 75.0, 25.0, 1.9, 'FOODSUPPLY-VENDOR-789'),
('Hähnchen', 'kg', 40.0, 15.0, 7.5, 'FOODSUPPLY-VENDOR-789'),
('Lachsfilet', 'kg', 25.0, 10.0, 15.5, 'FOODSUPPLY-VENDOR-123'),
('Reis', 'kg', 90.0, 25.0, 2.2, 'FOODSUPPLY-VENDOR-456');

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
