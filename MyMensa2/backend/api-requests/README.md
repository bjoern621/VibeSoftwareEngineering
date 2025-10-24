# MyMensa2 Bruno API Test Collection

Umfassende API-Testsuite für das MyMensa2 Backend-System.

## 📁 Struktur

Die Test-Collection ist in logische Ordner unterteilt:

### 🍽️ Meals

Verwaltung von Gerichten mit Soft-Delete-Pattern

-   `01-Get All Active Meals.bru` - Alle aktiven Gerichte abrufen
-   `02-Create New Meal.bru` - Neues Gericht erstellen
-   `03-Update Meal.bru` - Gericht aktualisieren
-   `04-Delete Meal (Soft Delete).bru` - Gericht löschen (Soft Delete)

### 📅 MealPlans

Speiseplan-Verwaltung mit Composite-Key-Pattern

-   `01-Get MealPlans For Date Range.bru` - Speiseplan für Zeitraum abrufen
-   `02-Add Meal To MealPlan.bru` - Gericht zum Speiseplan hinzufügen
-   `03-Update MealPlan Stock.bru` - Lagerbestand aktualisieren
-   `04-Remove Meal From MealPlan.bru` - Gericht aus Speiseplan entfernen

### 🛒 Orders

Bestellverwaltung mit QR-Code-Validierung und EASYPAY-Integration

-   `01-Get All Orders.bru` - Alle Bestellungen abrufen
-   `02-Get Orders Filtered.bru` - Gefilterte Bestellungen abrufen
-   `03-Create Order.bru` - Bestellung erstellen
-   `04-Pay Order.bru` - Bestellung bezahlen (EASYPAY)
-   `05-Validate Order QR Code.bru` - QR-Code validieren (Mensa-Mitarbeiter)

### 📊 Dashboard

Finanz- und Verkaufsdaten

-   `01-Get Dashboard Data.bru` - Dashboard-Daten abrufen
-   `02-Get Dashboard Data With Date Range.bru` - Dashboard mit Datumsfilter

### 📦 Inventory

Lagerverwaltung mit FOODSUPPLY-Integration

-   `01-Get All Ingredients.bru` - Alle Zutaten abrufen
-   `02-Get Low Stock Ingredients.bru` - Zutaten unter Mindestbestand
-   `03-Create Ingredient.bru` - Neue Zutat erstellen
-   `04-Update Ingredient Stock.bru` - Lagerbestand aktualisieren
-   `05-Trigger Automatic Reorder.bru` - Automatische Nachbestellung
-   `06-Consume Ingredients For Meal.bru` - Zutaten für Gerichtszubereitung verbrauchen

### 👥 Staff

Personalverwaltung mit STAFFMAN-Integration

-   `01-Get All Staff.bru` - Alle Mitarbeiter abrufen
-   `02-Get Staff By Role.bru` - Mitarbeiter nach Rolle filtern
-   `03-Get Available Staff.bru` - Verfügbare Mitarbeiter abrufen
-   `04-Create Staff Member.bru` - Neuen Mitarbeiter erstellen
-   `05-Record Working Hours.bru` - Arbeitszeiten erfassen
-   `06-Get Working Hours For Date Range.bru` - Arbeitszeiten für Zeitraum
-   `07-Get Working Hours For Specific Staff.bru` - Arbeitszeiten für Mitarbeiter
-   `08-Get Schedule Recommendation.bru` - Einsatzplanungs-Empfehlung

### 📈 Forecasts

KI-basierte Prognosen zur Reduktion von Lebensmittelverschwendung

-   `01-Get Demand Forecast.bru` - Wareneinsatz-Prognose

### 📋 Reports

Berichte und Nachhaltigkeit

-   `01-Get Sustainability Report.bru` - Nachhaltigkeitsbericht

### ⚠️ ErrorHandling

Fehlerbehandlungs-Tests

-   `01-Get Non-Existent Meal.bru` - 404 Fehler testen
-   `02-Create Meal With Invalid Data.bru` - 400 Fehler testen
-   `03-Invalid Date Format.bru` - Ungültiges Datumsformat
-   `04-Validate Invalid QR Code.bru` - Ungültiger QR-Code

### 🔄 Integration

End-to-End-Tests

-   `01-Complete Order Flow.bru` - Kompletter Bestellablauf
-   `02-Complete Meal Management Flow.bru` - Kompletter Gerichte-Verwaltungsablauf

## 🚀 Verwendung

### Voraussetzungen

1. Bruno API Client installiert
2. MyMensa2 Backend läuft auf `http://localhost:8081`

### Tests ausführen

1. Bruno öffnen
2. Collection öffnen: `api-requests` Ordner
3. Einzelne Tests ausführen oder gesamte Collection durchlaufen

### Test-Reihenfolge für Integrationstests

Für vollständige End-to-End-Tests empfohlene Reihenfolge:

1. **Gerichte-Management**
    - Create New Meal
    - Get All Active Meals
    - Update Meal
2. **Speiseplan**
    - Add Meal To MealPlan
    - Get MealPlans For Date Range
    - Update MealPlan Stock
3. **Bestellung & Zahlung**
    - Create Order
    - Pay Order
    - Validate Order QR Code
4. **Dashboard & Berichte**
    - Get Dashboard Data
    - Get Sustainability Report

## 📝 Hinweise

### Testdaten

-   Tests nutzen vorhandene Testdaten aus `data.sql`
-   Einige Tests erstellen neue Daten (z.B. Create-Operationen)
-   Soft Delete: Gelöschte Gerichte bleiben in DB, sind aber markiert

### Externe Systeme (Mock)

-   **EASYPAY**: Zahlungsintegration (simuliert)
-   **FOODSUPPLY**: Nachbestellung (simuliert)
-   **STAFFMAN**: Personalverwaltung (simuliert)

### Erwartete Responses

Alle Tests enthalten Assertions für:

-   HTTP Status Codes
-   Response-Struktur
-   Erforderliche Felder
-   Datentypen
-   Business-Logic-Validierungen

## 🔧 Anpassungen

### Base URL ändern

Falls Backend auf anderem Port läuft, URL in jedem Test anpassen:

```
http://localhost:8081 → http://localhost:DEIN_PORT
```

### Testdaten anpassen

-   IDs in Tests entsprechen den Daten aus `data.sql`
-   Bei Änderungen in `data.sql` müssen Test-IDs angepasst werden

## 📖 Weitere Dokumentation

-   Backend API Dokumentation: siehe `.github/copilot-instructions.md`
-   Architektur: Clean Architecture (3-Schichten-Modell)
-   Tech Stack: Spring Boot 3.5.6, Java 25, H2 Database

## ✅ Test Coverage

-   ✅ Gerichte-Verwaltung (CRUD + Soft Delete)
-   ✅ Speiseplan-Verwaltung (Composite Key)
-   ✅ Bestellverwaltung (QR-Code + EASYPAY)
-   ✅ Dashboard & Berichte
-   ✅ Lagerverwaltung (FOODSUPPLY)
-   ✅ Personalverwaltung (STAFFMAN)
-   ✅ Prognosen & Nachhaltigkeit
-   ✅ Fehlerbehandlung
-   ✅ End-to-End-Integration

## 🎯 Best Practices

1. Tests in logischer Reihenfolge ausführen
2. Nach Create-Tests entsprechende Get-Tests ausführen
3. Soft-Delete-Tests am Ende (um Testdaten zu erhalten)
4. Integration-Tests für vollständige Workflows nutzen
