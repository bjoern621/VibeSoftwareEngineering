-- Beispiel-Gerichte für MyMensa

-- Gericht 1: Spaghetti Bolognese
INSERT INTO meals (id, name, description, price, cost, ingredients, calories, protein, carbs, fat, deleted) 
VALUES (1, 'Spaghetti Bolognese', 'Italienische Pasta mit Hackfleischsauce', 6.50, 3.20, 'Nudeln, Hackfleisch, Tomatensauce, Zwiebeln', 650, 28.5, 75.0, 18.3, false);

INSERT INTO meal_categories (meal_id, category) VALUES (1, 'Vegetarisch');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (1, 'Gluten');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (1, 'Milch/Laktose');

-- Gericht 2: Veganer Burger
INSERT INTO meals (id, name, description, price, cost, ingredients, calories, protein, carbs, fat, deleted) 
VALUES (2, 'Veganer Burger', 'Burger mit pflanzlichem Patty', 7.90, 4.50, 'Veganes Patty, Vollkornbrötchen, Salat, Tomate, Gurke', 480, 22.0, 52.0, 15.5, false);

INSERT INTO meal_categories (meal_id, category) VALUES (2, 'Vegan');
INSERT INTO meal_categories (meal_id, category) VALUES (2, 'Vegetarisch');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (2, 'Gluten');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (2, 'Soja');

-- Gericht 3: Hähnchen-Curry
INSERT INTO meals (id, name, description, price, cost, ingredients, calories, protein, carbs, fat, deleted) 
VALUES (3, 'Hähnchen-Curry', 'Würziges Curry mit Reis', 8.50, 4.80, 'Hähnchenbrust, Reis, Curry-Sauce, Gemüse', 720, 35.0, 68.0, 22.0, false);

INSERT INTO meal_categories (meal_id, category) VALUES (3, 'Halal');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (3, 'Milch/Laktose');

-- Gericht 4: Caesar Salad
INSERT INTO meals (id, name, description, price, cost, ingredients, calories, protein, carbs, fat, deleted) 
VALUES (4, 'Caesar Salad', 'Frischer Salat mit Parmesan und Croutons', 5.90, 2.80, 'Römersalat, Parmesan, Croutons, Caesar-Dressing', 380, 12.5, 28.0, 24.0, false);

INSERT INTO meal_categories (meal_id, category) VALUES (4, 'Vegetarisch');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (4, 'Gluten');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (4, 'Milch/Laktose');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (4, 'Eier');

-- Gericht 5: Glutenfreie Pizza
INSERT INTO meals (id, name, description, price, cost, ingredients, calories, protein, carbs, fat, deleted) 
VALUES (5, 'Glutenfreie Pizza', 'Pizza mit glutenfreiem Teig', 9.50, 5.20, 'Glutenfreier Teig, Tomaten, Mozzarella, Basilikum', 580, 25.0, 62.0, 20.5, false);

INSERT INTO meal_categories (meal_id, category) VALUES (5, 'Glutenfrei');
INSERT INTO meal_categories (meal_id, category) VALUES (5, 'Vegetarisch');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (5, 'Milch/Laktose');

-- Gericht 6: Tomatensuppe
INSERT INTO meals (id, name, description, price, cost, ingredients, calories, protein, carbs, fat, deleted) 
VALUES (6, 'Tomatensuppe', 'Cremige Tomatensuppe mit frischen Kräutern', 4.50, 2.10, 'Tomaten, Sahne, Zwiebeln, Knoblauch, Basilikum', 280, 6.5, 32.0, 12.0, false);

INSERT INTO meal_categories (meal_id, category) VALUES (6, 'Vegetarisch');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (6, 'Milch/Laktose');

-- Gericht 7: Fischfilet mit Reis
INSERT INTO meals (id, name, description, price, cost, ingredients, calories, protein, carbs, fat, deleted) 
VALUES (7, 'Fischfilet mit Reis', 'Gebratenes Fischfilet mit Basmatireis und Gemüse', 9.90, 5.80, 'Seelachsfilet, Reis, Brokkoli, Karotten, Zitrone', 520, 38.0, 55.0, 12.5, false);

INSERT INTO meal_categories (meal_id, category) VALUES (7, 'Glutenfrei');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (7, 'Fisch');

-- Gericht 8: Schnitzel mit Pommes
INSERT INTO meals (id, name, description, price, cost, ingredients, calories, protein, carbs, fat, deleted) 
VALUES (8, 'Schnitzel mit Pommes', 'Knuspriges Schweineschnitzel mit goldenen Pommes', 8.90, 4.90, 'Schweineschnitzel, Paniermehl, Kartoffeln, Öl', 820, 42.0, 68.0, 38.0, false);

INSERT INTO meal_categories (meal_id, category) VALUES (8, 'Halal');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (8, 'Gluten');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (8, 'Eier');

-- Gericht 9: Vegane Bowl
INSERT INTO meals (id, name, description, price, cost, ingredients, calories, protein, carbs, fat, deleted) 
VALUES (9, 'Vegane Bowl', 'Quinoa-Bowl mit Kichererbsen und Avocado', 7.50, 3.90, 'Quinoa, Kichererbsen, Avocado, Spinat, Paprika', 450, 18.0, 52.0, 16.5, false);

INSERT INTO meal_categories (meal_id, category) VALUES (9, 'Vegan');
INSERT INTO meal_categories (meal_id, category) VALUES (9, 'Vegetarisch');
INSERT INTO meal_categories (meal_id, category) VALUES (9, 'Glutenfrei');

-- Gericht 10: Lasagne
INSERT INTO meals (id, name, description, price, cost, ingredients, calories, protein, carbs, fat, deleted) 
VALUES (10, 'Lasagne', 'Klassische italienische Lasagne mit Béchamelsauce', 7.80, 4.20, 'Lasagneplatten, Hackfleisch, Tomatensauce, Béchamelsauce, Käse', 680, 32.0, 58.0, 28.5, false);

INSERT INTO meal_categories (meal_id, category) VALUES (10, 'Vegetarisch');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (10, 'Gluten');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (10, 'Milch/Laktose');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (10, 'Eier');

-- Gericht 11: Apfelstrudel
INSERT INTO meals (id, name, description, price, cost, ingredients, calories, protein, carbs, fat, deleted) 
VALUES (11, 'Apfelstrudel', 'Hausgemachter Apfelstrudel mit Vanillesauce', 3.90, 1.80, 'Äpfel, Blätterteig, Zimt, Rosinen, Vanillesauce', 320, 5.5, 48.0, 12.0, false);

INSERT INTO meal_categories (meal_id, category) VALUES (11, 'Vegetarisch');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (11, 'Gluten');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (11, 'Milch/Laktose');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (11, 'Eier');

-- Gericht 12: Schokoladenmousse
INSERT INTO meals (id, name, description, price, cost, ingredients, calories, protein, carbs, fat, deleted) 
VALUES (12, 'Schokoladenmousse', 'Cremige Schokoladenmousse', 3.50, 1.50, 'Dunkle Schokolade, Sahne, Eier, Zucker', 380, 6.0, 35.0, 24.0, false);

INSERT INTO meal_categories (meal_id, category) VALUES (12, 'Vegetarisch');
INSERT INTO meal_categories (meal_id, category) VALUES (12, 'Glutenfrei');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (12, 'Milch/Laktose');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (12, 'Eier');

-- Gericht 13: Gemüsepfanne
INSERT INTO meals (id, name, description, price, cost, ingredients, calories, protein, carbs, fat, deleted) 
VALUES (13, 'Gemüsepfanne', 'Bunte Gemüsepfanne mit Reis', 6.90, 3.50, 'Paprika, Zucchini, Aubergine, Reis, Sojasauce', 420, 12.0, 68.0, 8.5, false);

INSERT INTO meal_categories (meal_id, category) VALUES (13, 'Vegan');
INSERT INTO meal_categories (meal_id, category) VALUES (13, 'Vegetarisch');
INSERT INTO meal_categories (meal_id, category) VALUES (13, 'Glutenfrei');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (13, 'Soja');

-- Gericht 14: Rindergulasch
INSERT INTO meals (id, name, description, price, cost, ingredients, calories, protein, carbs, fat, deleted) 
VALUES (14, 'Rindergulasch', 'Deftiges Rindergulasch mit Spätzle', 9.50, 5.50, 'Rindfleisch, Paprika, Zwiebeln, Spätzle', 750, 45.0, 62.0, 28.0, false);

INSERT INTO meal_categories (meal_id, category) VALUES (14, 'Halal');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (14, 'Gluten');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (14, 'Eier');

-- Gericht 15: Tiramisu
INSERT INTO meals (id, name, description, price, cost, ingredients, calories, protein, carbs, fat, deleted) 
VALUES (15, 'Tiramisu', 'Italienisches Tiramisu mit Mascarpone', 4.50, 2.20, 'Löffelbiskuits, Mascarpone, Kaffee, Kakao, Eier', 420, 8.5, 42.0, 22.0, false);

INSERT INTO meal_categories (meal_id, category) VALUES (15, 'Vegetarisch');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (15, 'Gluten');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (15, 'Milch/Laktose');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (15, 'Eier');

-- ============================================
-- MealPlans (Speiseplan) - Beispieldaten
-- ============================================

-- KW 42 - Aktuelle Woche (13.10. - 17.10.2025)
-- Montag (2025-10-13)
INSERT INTO meal_plans (meal_id, date, stock) VALUES (1, '2025-10-13', 50);   -- Spaghetti Bolognese
INSERT INTO meal_plans (meal_id, date, stock) VALUES (2, '2025-10-13', 30);   -- Veganer Burger
INSERT INTO meal_plans (meal_id, date, stock) VALUES (6, '2025-10-13', 40);   -- Tomatensuppe
INSERT INTO meal_plans (meal_id, date, stock) VALUES (11, '2025-10-13', 25);  -- Apfelstrudel

-- Dienstag (2025-10-14) - Heute
INSERT INTO meal_plans (meal_id, date, stock) VALUES (3, '2025-10-14', 45);   -- Hähnchen-Curry
INSERT INTO meal_plans (meal_id, date, stock) VALUES (7, '2025-10-14', 35);   -- Fischfilet mit Reis
INSERT INTO meal_plans (meal_id, date, stock) VALUES (9, '2025-10-14', 30);   -- Vegane Bowl
INSERT INTO meal_plans (meal_id, date, stock) VALUES (12, '2025-10-14', 20);  -- Schokoladenmousse

-- Mittwoch (2025-10-15)
INSERT INTO meal_plans (meal_id, date, stock) VALUES (8, '2025-10-15', 50);   -- Schnitzel mit Pommes
INSERT INTO meal_plans (meal_id, date, stock) VALUES (10, '2025-10-15', 40);  -- Lasagne
INSERT INTO meal_plans (meal_id, date, stock) VALUES (4, '2025-10-15', 30);   -- Caesar Salad

-- Donnerstag (2025-10-16)
INSERT INTO meal_plans (meal_id, date, stock) VALUES (14, '2025-10-16', 45);  -- Rindergulasch
INSERT INTO meal_plans (meal_id, date, stock) VALUES (5, '2025-10-16', 35);   -- Glutenfreie Pizza
INSERT INTO meal_plans (meal_id, date, stock) VALUES (13, '2025-10-16', 30);  -- Gemüsepfanne
INSERT INTO meal_plans (meal_id, date, stock) VALUES (15, '2025-10-16', 25);  -- Tiramisu

-- Freitag (2025-10-17)
INSERT INTO meal_plans (meal_id, date, stock) VALUES (1, '2025-10-17', 55);   -- Spaghetti Bolognese
INSERT INTO meal_plans (meal_id, date, stock) VALUES (7, '2025-10-17', 40);   -- Fischfilet mit Reis
INSERT INTO meal_plans (meal_id, date, stock) VALUES (2, '2025-10-17', 35);   -- Veganer Burger
INSERT INTO meal_plans (meal_id, date, stock) VALUES (11, '2025-10-17', 30);  -- Apfelstrudel

-- ============================================
-- KW 43 - Nächste Woche (20.10. - 24.10.2025)
-- ============================================

-- Montag (2025-10-20)
INSERT INTO meal_plans (meal_id, date, stock) VALUES (3, '2025-10-20', 50);   -- Hähnchen-Curry
INSERT INTO meal_plans (meal_id, date, stock) VALUES (9, '2025-10-20', 40);   -- Vegane Bowl
INSERT INTO meal_plans (meal_id, date, stock) VALUES (6, '2025-10-20', 35);   -- Tomatensuppe
INSERT INTO meal_plans (meal_id, date, stock) VALUES (12, '2025-10-20', 25);  -- Schokoladenmousse

-- Dienstag (2025-10-21)
INSERT INTO meal_plans (meal_id, date, stock) VALUES (8, '2025-10-21', 55);   -- Schnitzel mit Pommes
INSERT INTO meal_plans (meal_id, date, stock) VALUES (5, '2025-10-21', 40);   -- Glutenfreie Pizza
INSERT INTO meal_plans (meal_id, date, stock) VALUES (4, '2025-10-21', 30);   -- Caesar Salad

-- Mittwoch (2025-10-22)
INSERT INTO meal_plans (meal_id, date, stock) VALUES (10, '2025-10-22', 50);  -- Lasagne
INSERT INTO meal_plans (meal_id, date, stock) VALUES (7, '2025-10-22', 40);   -- Fischfilet mit Reis
INSERT INTO meal_plans (meal_id, date, stock) VALUES (13, '2025-10-22', 35);  -- Gemüsepfanne
INSERT INTO meal_plans (meal_id, date, stock) VALUES (15, '2025-10-22', 30);  -- Tiramisu

-- Donnerstag (2025-10-23)
INSERT INTO meal_plans (meal_id, date, stock) VALUES (14, '2025-10-23', 45);  -- Rindergulasch
INSERT INTO meal_plans (meal_id, date, stock) VALUES (2, '2025-10-23', 40);   -- Veganer Burger
INSERT INTO meal_plans (meal_id, date, stock) VALUES (6, '2025-10-23', 35);   -- Tomatensuppe
INSERT INTO meal_plans (meal_id, date, stock) VALUES (11, '2025-10-23', 25);  -- Apfelstrudel

-- Freitag (2025-10-24)
INSERT INTO meal_plans (meal_id, date, stock) VALUES (1, '2025-10-24', 60);   -- Spaghetti Bolognese
INSERT INTO meal_plans (meal_id, date, stock) VALUES (9, '2025-10-24', 45);   -- Vegane Bowl
INSERT INTO meal_plans (meal_id, date, stock) VALUES (5, '2025-10-24', 35);   -- Glutenfreie Pizza
INSERT INTO meal_plans (meal_id, date, stock) VALUES (12, '2025-10-24', 30);  -- Schokoladenmousse

-- Beispiel-Bestellungen (Optional für Tests)
-- Bestellung 1: Bezahlt mit QR-Code, noch nicht abgeholt
INSERT INTO orders (id, meal_id, date, paid, qr_code, collected, created_at) 
VALUES (1, 1, '2025-10-15', true, 'ORDER-1', false, '2025-10-14T10:30:00');

-- Bestellung 2: Bezahlt mit QR-Code, bereits abgeholt
INSERT INTO orders (id, meal_id, date, paid, qr_code, collected, collected_at, created_at) 
VALUES (2, 2, '2025-10-15', true, 'ORDER-2', true, '2025-10-15T12:15:00', '2025-10-14T11:00:00');

-- Bestellung 3: Noch nicht bezahlt
INSERT INTO orders (id, meal_id, date, paid, collected, created_at) 
VALUES (3, 3, '2025-10-16', false, false, '2025-10-14T14:20:00');

-- Reset Auto-Increment-Sequenzen für IDs
-- H2 verwendet IDENTITY-Spalten, daher müssen wir die Sequenz manuell setzen
ALTER TABLE meals ALTER COLUMN id RESTART WITH 16;
ALTER TABLE orders ALTER COLUMN id RESTART WITH 4;
