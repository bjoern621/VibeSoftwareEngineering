# Integration der Mock-Daten - Übersicht

## ✅ Erstellt

### Services
1. **mockData.js** - Umfangreiche Mock-Daten für alle Bereiche
   - 8 Gerichte mit verschiedenen Kategorien (vegan, vegetarisch, halal, glutenfrei, standard)
   - Speisepläne für 3 Tage mit realistischen Beständen
   - 6 Bestellungen mit verschiedenen Stati (bezahlt/unbezahlt, abgeholt/nicht abgeholt)
   - Dashboard-Aggregationen (Einnahmen, Kosten, beliebte Gerichte)
   - 8 Lagerartikel mit Mindestbeständen
   - 6 Mitarbeitende mit verschiedenen Rollen und Status

2. **api.js** - Zentraler API-Service-Layer
   - Einfaches Umschalten zwischen Mock und echtem Backend via `USE_MOCK_DATA`
   - Simulierte Netzwerk-Verzögerung (300ms)
   - Alle CRUD-Operationen für: Meals, MealPlans, Orders, Dashboard, Inventory, Staff
   - Fehlerbehandlung
   - Automatische Bestandsverwaltung bei Bestellungen

## 🔄 Zu aktualisieren

### Komponenten (Import von api.js hinzufügen):

1. **OrderManagement.js** - ✅ ERLEDIGT
   - `api.mealPlans.getByDate()`
   - `api.orders.create()`

2. **Dashboard.js**
   - `api.dashboard.getData(startDate, endDate)`

3. **MealManagement.js**
   - `api.meals.getAll()`
   - `api.meals.create()`
   - `api.meals.update()`
   - `api.meals.delete()`

4. **MealPlanManagement.js**
   - `api.meals.getAll()` (für Dropdown)
   - `api.mealPlans.getByDate()`
   - `api.mealPlans.create()`
   - `api.mealPlans.update()`
   - `api.mealPlans.delete()`

5. **AdminOrderManagement.js**
   - `api.orders.getAll(filters)`
   - `api.orders.markAsPaid()`
   - `api.orders.markAsCollected()`
   - `api.orders.delete()`

6. **InventoryManagement.js**
   - `api.inventory.getAll()`
   - `api.inventory.reorder()`

7. **StaffManagement.js**
   - `api.staff.getAll()`
   - `api.staff.sync()`

## 🎯 Vorteile

### Entwicklung
- **Sofortige Testbarkeit**: Keine Abhängigkeit vom Backend
- **Schnelle Iteration**: Keine Wartezeiten durch Backend-Deployments
- **Offline-Entwicklung**: Arbeiten ohne Netzwerk/Backend möglich

### Testing
- **Konsistente Daten**: Reproduzierbare Testszenarien
- **Edge Cases**: Einfach verschiedene Szenarien testen (leere Listen, volle Bestände, etc.)
- **Performance**: Instant-Feedback ohne echte API-Calls

### Produktion
- **Einfacher Umschalter**: `USE_MOCK_DATA = false` → echtes Backend
- **Gleiche API-Struktur**: Keine Code-Änderungen in Komponenten nötig
- **Schrittweise Migration**: Einzelne Endpoints nacheinander umstellen

## 🔧 Verwendung

### Mock-Modus (aktuell aktiv)
```javascript
// In src/services/api.js
const USE_MOCK_DATA = true;
```

### Produktions-Modus (echtes Backend)
```javascript
// In src/services/api.js
const USE_MOCK_DATA = false;
```

### In Komponenten
```javascript
import api from '../services/api';

// Statt:
const response = await fetch('/api/meals');
const data = await response.json();

// Jetzt:
const data = await api.meals.getAll();
```

## 📊 Mock-Daten-Details

### Gerichte
- ID 1-8
- Verschiedene Kategorien (vegan, vegetarisch, halal, glutenfrei, standard)
- Realistische Preise (5.20€ - 8.50€)
- Kosten (1.90€ - 4.20€)
- Vollständige Nährwertangaben
- Allergene-Listen

### Speisepläne
- 22.10.2025: 4 Gerichte
- 23.10.2025: 4 Gerichte
- 24.10.2025: 4 Gerichte
- Bestände: 22-50 Portionen

### Bestellungen
- 6 Bestellungen für heute (22.10.2025)
- Verschiedene Stati: 2 ausstehend, 4 bezahlt, 2 abgeholt
- QR-Codes im Format: QR-2025-XXX-XXXX

### Dashboard
- Gesamteinnahmen: 487.50€
- Gesamtkosten: 198.20€
- Gewinn: 289.30€
- 28 Bestellungen
- Top 5 beliebteste Gerichte

### Lager
- 8 Artikel
- 2 unter Mindestbestand (Kartoffeln, Hähnchenbrust)
- Verschiedene Kategorien (Gemüse, Fleisch, Getreide, etc.)

### Personal
- 6 Mitarbeitende
- 3 Köche, 3 Servicekräfte
- Status: 4 verfügbar, 1 Urlaub, 1 krank
- Arbeitszeiten: 30-42h/Woche

## ✨ Nächste Schritte
1. Alle Komponenten auf api.js umstellen
2. CSS-Dateien im minimalistischen Design aktualisieren
3. Anforderungen prüfen (QR-Code-Anzeige, EASYPAY-Integration, etc.)
4. Testing mit Mock-Daten
5. Backend-Integration vorbereiten
