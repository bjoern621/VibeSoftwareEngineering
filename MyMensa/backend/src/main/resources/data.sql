-- Beispiel-Gerichte für MyMensa

-- Gericht 1: Spaghetti Bolognese
INSERT INTO meals (id, name, description, price, cost,  ingredients, calories, protein, carbs, fat) 
VALUES (1, 'Spaghetti Bolognese', 'Italienische Pasta mit Hackfleischsauce', 6.50, 3.20, 'Nudeln, Hackfleisch, Tomatensauce, Zwiebeln', 650, 28.5, 75.0, 18.3);

INSERT INTO meal_categories (meal_id, category) VALUES (1, 'Vegetarisch');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (1, 'Gluten');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (1, 'Milch/Laktose');

-- Gericht 2: Veganer Burger
INSERT INTO meals (id, name, description, price, cost,  ingredients, calories, protein, carbs, fat) 
VALUES (2, 'Veganer Burger', 'Burger mit pflanzlichem Patty', 7.90, 4.50, 'Veganes Patty, Vollkornbrötchen, Salat, Tomate, Gurke', 480, 22.0, 52.0, 15.5);

INSERT INTO meal_categories (meal_id, category) VALUES (2, 'Vegan');
INSERT INTO meal_categories (meal_id, category) VALUES (2, 'Vegetarisch');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (2, 'Gluten');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (2, 'Soja');

-- Gericht 3: Hähnchen-Curry
INSERT INTO meals (id, name, description, price, cost,  ingredients, calories, protein, carbs, fat) 
VALUES (3, 'Hähnchen-Curry', 'Würziges Curry mit Reis', 8.50, 4.80,  'Hähnchenbrust, Reis, Curry-Sauce, Gemüse', 720, 35.0, 68.0, 22.0);

INSERT INTO meal_categories (meal_id, category) VALUES (3, 'Halal');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (3, 'Milch/Laktose');

-- Gericht 4: Caesar Salad
INSERT INTO meals (id, name, description, price, cost,  ingredients, calories, protein, carbs, fat) 
VALUES (4, 'Caesar Salad', 'Frischer Salat mit Parmesan und Croutons', 5.90, 2.80, 'Römersalat, Parmesan, Croutons, Caesar-Dressing', 380, 12.5, 28.0, 24.0);

INSERT INTO meal_categories (meal_id, category) VALUES (4, 'Vegetarisch');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (4, 'Gluten');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (4, 'Milch/Laktose');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (4, 'Eier');

-- Gericht 5: Glutenfreie Pizza
INSERT INTO meals (id, name, description, price, cost,  ingredients, calories, protein, carbs, fat) 
VALUES (5, 'Glutenfreie Pizza', 'Pizza mit glutenfreiem Teig', 9.50, 5.20, 'Glutenfreier Teig, Tomaten, Mozzarella, Basilikum', 580, 25.0, 62.0, 20.5);

INSERT INTO meal_categories (meal_id, category) VALUES (5, 'Glutenfrei');
INSERT INTO meal_categories (meal_id, category) VALUES (5, 'Vegetarisch');
INSERT INTO meal_allergens (meal_id, allergen) VALUES (5, 'Milch/Laktose');
