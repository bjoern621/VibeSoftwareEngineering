# MyMensa2 Frontend - Mock-Daten System

## 📦 Was wurde erstellt?

### 1. Mock-Daten Service (`src/services/mockData.js`)

Umfangreiche, realistische Testdaten für alle Bereiche:

#### Gerichte (8 Stück)
- **Spaghetti Carbonara** (Standard, 6.50€)
- **Gemüse-Curry** (Vegan, 5.90€)
- **Hähnchen-Schnitzel** (Halal, 7.20€)
- **Linsen-Dal** (Vegan, 5.50€)
- **Quinoa-Salat** (Vegetarisch, 6.80€)
- **Rindergulasch** (Standard, 7.90€)
- **Falafel-Wrap** (Vegan, 5.20€)
- **Lachsfilet** (Glutenfrei, 8.50€)

Jedes Gericht enthält:
- Vollständige Nährwertangaben (Kalorien, Protein, Kohlenhydrate, Fett)
- Preis und Kosten
- Kategorie (vegan, vegetarisch, halal, glutenfrei, standard)
- Allergene-Liste

#### Speisepläne (3 Tage)
- **22.10.2025**: 4 Gerichte (25-45 Portionen)
- **23.10.2025**: 4 Gerichte (22-40 Portionen)
- **24.10.2025**: 4 Gerichte (28-50 Portionen)

#### Bestellungen (6 Stück)
- 2 ausstehende Zahlungen
- 2 bezahlt, noch nicht abgeholt
- 2 komplett abgeschlossen
- Alle mit realistischen QR-Codes (z.B. `QR-2025-001-A7B3`)

#### Dashboard-Aggregationen
- **Gesamteinnahmen**: 487.50€
- **Gesamtkosten**: 198.20€
- **Gewinn**: 289.30€
- **Bestellungen**: 28
- **Top 5 Gerichte**: Mit Bestellanzahl

#### Lagerbestände (8 Artikel)
- Tomaten, Kartoffeln, Reis, Hähnchenbrust, Olivenöl, Milch, Linsen, Zwiebeln
- 2 Artikel unter Mindestbestand (⚠️ Kartoffeln, Hähnchenbrust)
- Verschiedene Kategorien und Lieferanten

#### Personal (6 Mitarbeitende)
- 3 Köche, 3 Servicekräfte
- Status: 4 verfügbar, 1 Urlaub, 1 krank
- Arbeitszeiten: 30-42h/Woche
- Kontaktdaten (E-Mail, Telefon)

### 2. API Service Layer (`src/services/api.js`)

Zentralisierte API mit einfachem Umschalter:

```javascript
// Mock-Modus (aktuell)
const USE_MOCK_DATA = true;

// Produktions-Modus (später)
const USE_MOCK_DATA = false;
```

#### Verfügbare APIs:
- `api.meals` - CRUD für Gerichte
- `api.mealPlans` - Speiseplan-Verwaltung
- `api.orders` - Bestellverwaltung (inkl. Zahlung/Abholung)
- `api.dashboard` - Finanzberichte
- `api.inventory` - Lagerverwaltung (inkl. Nachbestellung)
- `api.staff` - Personalverwaltung (inkl. STAFFMAN-Sync)

#### Features:
- ✅ Simulierte Netzwerk-Verzögerung (300ms)
- ✅ Realistische Fehlerbehandlung
- ✅ Automatische Bestandsverwaltung bei Bestellungen
- ✅ QR-Code-Generierung
- ✅ ID-Verwaltung für neue Einträge

## 🔄 Komponenten-Integration

### ✅ Bereits integriert:

1. **OrderManagement.js**
   - `api.mealPlans.getByDate()` - Speiseplan laden
   - `api.orders.create()` - Bestellung aufgeben
   
2. **Dashboard.js**
   - `api.dashboard.getData()` - Dashboard-Daten laden

### 📝 Noch zu integrieren:

3. **MealManagement.js**
   ```javascript
   import api from '../services/api';
   
   // Gerichte laden
   const meals = await api.meals.getAll();
   
   // Neues Gericht
   const newMeal = await api.meals.create(mealData);
   
   // Aktualisieren
   await api.meals.update(id, mealData);
   
   // Löschen (Soft Delete)
   await api.meals.delete(id);
   ```

4. **MealPlanManagement.js**
   ```javascript
   // Gerichte für Dropdown
   const meals = await api.meals.getAll();
   
   // Speiseplan
   const plans = await api.mealPlans.getByDate(date);
   
   // Hinzufügen
   await api.mealPlans.create({ mealId, date, stock });
   
   // Bestand ändern
   await api.mealPlans.update(mealId, { date, stock });
   
   // Entfernen
   await api.mealPlans.delete(mealId, date);
   ```

5. **AdminOrderManagement.js**
   ```javascript
   // Alle Bestellungen (mit Filter)
   const orders = await api.orders.getAll({ date, status });
   
   // Zahlung markieren
   await api.orders.markAsPaid(orderId);
   
   // Abholung markieren
   await api.orders.markAsCollected(orderId);
   
   // Löschen
   await api.orders.delete(orderId);
   ```

6. **InventoryManagement.js**
   ```javascript
   // Lagerbestände
   const inventory = await api.inventory.getAll();
   
   // Nachbestellen (FOODSUPPLY)
   await api.inventory.reorder(itemId);
   ```

7. **StaffManagement.js**
   ```javascript
   // Personal
   const staff = await api.staff.getAll();
   
   // STAFFMAN synchronisieren
   await api.staff.sync();
   ```

## 🎯 Vorteile des Systems

### Für Entwicklung:
- ⚡ **Sofort lauffähig** - Kein Backend nötig
- 🚀 **Schnelle Iteration** - Keine API-Wartezeiten
- 📶 **Offline-fähig** - Entwickeln ohne Internet

### Für Testing:
- 🎲 **Konsistente Daten** - Reproduzierbare Tests
- 🔬 **Edge Cases** - Einfach verschiedene Szenarien testen
- ⚡ **Instant Feedback** - Keine echten API-Calls

### Für Produktion:
- 🔄 **Ein Schalter** - `USE_MOCK_DATA = false`
- 🏗️ **Gleiche Struktur** - Keine Code-Änderungen in Komponenten
- 📈 **Schrittweise** - Einzelne Endpoints nacheinander umstellen

## 🛠️ Verwendung

### Beispiel: Gerichte laden

**Vorher (direkt fetch):**
```javascript
const response = await fetch('/api/meals');
if (!response.ok) throw new Error('Fehler');
const meals = await response.json();
```

**Nachher (mit API-Service):**
```javascript
import api from '../services/api';

const meals = await api.meals.getAll();
// Fehlerbehandlung ist bereits integriert!
```

### Beispiel: Bestellung erstellen

**Vorher:**
```javascript
const response = await fetch('/api/orders', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ mealId, quantity, orderDate })
});
if (!response.ok) throw new Error('Fehler');
const order = await response.json();
```

**Nachher:**
```javascript
const order = await api.orders.create({ mealId, quantity, orderDate });
// QR-Code automatisch generiert, Bestand aktualisiert!
```

## 📊 Daten-Highlights

### Realistische Szenarien:

1. **Niedrige Lagerbestände**
   - Kartoffeln: 15kg (Min: 30kg) → ⚠️ Nachbestellung nötig
   - Hähnchenbrust: 8kg (Min: 15kg) → ⚠️ Nachbestellung nötig

2. **Verschiedene Bestellstati**
   - Offene Zahlung → Test von Zahlungserinnerungen
   - Bezahlt, nicht abgeholt → QR-Code-Validierung
   - Komplett abgeschlossen → Archivierung

3. **Personal-Verfügbarkeit**
   - 1 Mitarbeiter im Urlaub → Einsatzplanung
   - 1 Mitarbeiter krank → Vertretungsregelung
   - 4 verfügbar → Normale Planung

4. **Beliebte Gerichte**
   - Spaghetti Carbonara (12 Bestellungen) → Mehr einplanen
   - Falafel-Wrap (6 Bestellungen) → Stabiler Favorit

## ✨ Nächste Schritte

1. ✅ Mock-Daten-System erstellt
2. ✅ OrderManagement integriert
3. ✅ Dashboard integriert
4. ⏳ Restliche Komponenten integrieren
5. ⏳ Minimalistisches CSS finalisieren
6. ⏳ Anforderungen prüfen (QR-Code, EASYPAY, etc.)
7. ⏳ Backend-Connection testen
8. ⏳ `USE_MOCK_DATA = false` setzen

## 🔐 Umstellung auf echtes Backend

Wenn das Backend bereit ist:

1. In `src/services/api.js` ändern:
   ```javascript
   const USE_MOCK_DATA = false;
   ```

2. Fertig! Alle Komponenten funktionieren mit echtem Backend.

3. Optional: Schrittweise Umstellung möglich (einzelne APIs)

## 📝 Notizen

- Alle Preise in Euro (€)
- Alle Daten in ISO-Format (yyyy-MM-dd)
- QR-Codes im Format: QR-JAHR-NNN-XXXX
- Allergene als String-Array
- Soft Delete für Gerichte (deleted-Flag)
