# MyMensa2 – KI-Coding-Assistent Anweisungen

## Projektübersicht

MyMensa2 ist ein digitales Mensaverwaltungssystem mit Spring Boot Backend und React Frontend. Das System verwaltet Gerichte, Speisepläne, Bestellungen mit QR-Codes, Lagerverwaltung, Zahlungsintegration (EASYPAY) und bietet Finanz-Dashboards sowie Prognosen zur Reduktion von Lebensmittelverschwendung.

### Systemkontext

**MyMensa2** interagiert mit folgenden externen Systemen:

-   **EASYPAY**: Bezahldienstleister für Zahlungen via Kredit-/Debitkarte, Guthabenkonto, Bitcoin (keine Barzahlung)
-   **FOODSUPPLY**: Automatische Nachbestellung bei Lieferanten bei Unterschreitung von Mindestbeständen
-   **STAFFMAN**: Synchronisation von Arbeitszeiten der Mitarbeitenden und Einsatzplanung basierend auf Besucherzahlen

**Nutzergruppen**:

-   Studierende/Mitarbeitende: Vorbestellung via Mobile App, Abholung mit QR-Code nach Bezahlung
-   Mensa-Mitarbeitende: QR-Code-Validierung bei Essensausgabe über separate Mobile App
-   Mensaverwaltung: Portal für Speiseplan, Lagerbestände, Berichte, Gerichte- und Personalverwaltung

## Tech Stack

### Backend

-   **Framework**: Spring Boot 3.5.6
-   **Java-Version**: Java 25
-   **Datenbank**: H2 In-Memory
-   **Persistierung**: Spring Data JPA, Hibernate
-   **Build-Tool**: Maven

### Frontend

-   **Framework**: React 19.2.0
-   **Build-Tool**: Create React App (react-scripts 5.0.1)
-   **Dev-Server**: Port 3001 (MyMensa2)
-   **HTTP-Client**: Fetch API, Proxy zu Backend auf Port 8081

### Architektur

-   **Backend**: Clean Architecture (3-Schichten-Modell)
-   **Frontend**: Komponentenbasierte Architektur mit funktionalen React-Komponenten

## Architektur-Pattern: Clean Architecture (3-Schicht-Modell)

Das Backend folgt einem strikten **3-Schichten-Muster** mit klarer Trennung:

```
feature/
├── dataaccess/     # Entities (@Entity), Repositories (@Repository), Composite Keys
├── logic/          # Business-Services (@Service), @Transactional, Validierung
└── facade/         # REST-Controller (@RestController), DTOs (Java Records)
```

### Verantwortlichkeiten der Schichten

-   **dataaccess**: JPA-Entities mit `@Entity`, Repositories mit `@Repository`, Composite Keys
-   **logic**: Business-Services mit `@Service`, `@Transactional` für Transaktionen, Validierungslogik
-   **facade**: REST-Controller mit `@RestController`, DTOs als Java Records, HTTP-Layer-Logik

**Wichtige Konvention**: Niemals Schichtgrenzen falsch überschreiten. Controller rufen Services auf, Services nutzen Repositories. DTOs bleiben in der Facade-Schicht, Entities in Dataaccess.

## Wichtige technische Patterns

### 1. Soft Delete Pattern

Gerichte (Meals) verwenden Soft Delete statt harter Löschung, um historische Speiseplan-Daten zu bewahren:

```
@Column(nullable = false)
private Boolean deleted = false;

@Column
private LocalDateTime deletedAt;
```

Repositories müssen sowohl `findByIdActive()` (ohne gelöschte) als auch `findById()` (mit gelöschten) bereitstellen. Siehe `MealRepository` und `MealService.getMealByIdIncludingDeleted()`.

### 2. Composite Key Pattern

`MealPlan` nutzt einen zusammengesetzten Primärschlüssel (`@IdClass(MealPlanId.class)`) aus `mealId + date`. Die Klasse `MealPlanId` muss `Serializable`, `equals()` und `hashCode()` implementieren. Siehe `MealPlan.java` und `MealPlanId.java`.

### 3. Globale Exception-Behandlung

Alle Exceptions nutzen `@ControllerAdvice` in `GlobalExceptionHandler`:

-   `ResourceNotFoundException` → 404 mit strukturiertem Fehler-JSON
-   `InvalidRequestException` → 400 mit deutscher Fehlermeldung
-   `DataIntegrityViolationException` → 409 mit nutzerfreundlicher Constraint-Meldung

**Wichtig**: Wirf immer Custom-Exceptions, statt direkt Error-Responses zurückzugeben.

### 4. Datumsverarbeitung

-   Nutze `LocalDate` für reine Datumswerte (Speisepläne, Bestellungen)
-   Nutze `LocalDateTime` für Zeitstempel (Bestellerstellung, Abholung)
-   Frontend sendet Daten als `"yyyy-MM-dd"`-Strings, Backend parst mit `DateTimeFormatter.ofPattern("yyyy-MM-dd")`

### 5. DTO-Pattern mit Records

Nutze Java Records für DTOs (Java 17+):

```
public record MealPlanRequestDTO(Integer mealId, String date, Integer stock) {}
```

## Datenbank-Konfiguration

-   **H2 In-Memory** für Entwicklung (siehe `application.properties`)
-   **Schema-Erstellung**: `spring.jpa.hibernate.ddl-auto=create-drop`
-   **Testdaten**: Laden aus `data.sql` nach Schema-Erstellung (`spring.jpa.defer-datasource-initialization=true`)
-   **H2 Console**: `http://localhost:8080/h2-console` bzw. `http://localhost:8081/h2-console` (JDBC URL: `jdbc:h2:mem:mymensa` bzw. `jdbc:h2:mem:mymensa2`)

## Anwendung starten

### Backend

```
cd MyMensa2/backend
./mvnw spring-boot:run  # Läuft auf Port 8081
```

### Frontend

```
cd MyMensa2/frontend
npm start  # Läuft auf Port 3001
```

### CORS-Konfiguration

Alle REST-Controller nutzen `@CrossOrigin(origins = "http://localhost:3001")` für lokale Entwicklung von MyMensa2.

## Domain-Modell-Beziehungen

-   **Meal** (1) → (\*) **MealPlan**: Ein Gericht kann in mehreren Speiseplänen an verschiedenen Daten erscheinen
-   **Meal** (1) → (\*) **Order**: Ein Gericht kann mehrfach bestellt werden
-   **Order** hat QR-Code zur Abhol-Verifizierung und Zahlungstracking (`paid`, `collected` Flags)
-   **Dashboard** aggregiert Bestelldaten zur Berechnung von Einnahmen (price) vs. Ausgaben (cost)
-   **Inventory**: Lagerverwaltung mit Mindestbeständen, automatische Nachbestellung via FOODSUPPLY
-   **Ingredient**: Zutaten mit Bestandsverfolgung, Aktualisierung bei Zubereitung/Verkauf
-   **Staff**: Arbeitszeiten der Mitarbeitenden, Synchronisation mit STAFFMAN

## Frontend-Struktur

React-App mit komponentenbasierter Architektur:

-   **App.js**: Hauptnavigation zwischen OrderManagement und AdminPanel
-   **OrderManagement**: Kundenoberfläche zur Bestellaufgabe
-   **AdminPanel**: Admin-Hub mit Tabs für Dashboard, Meals, MealPlans, Orders, Inventory, Staff
-   Jede Admin-Funktion ist separate Komponente (MealManagement.js, Dashboard.js, InventoryManagement.js, etc.)

## Typ-Präzision

Backend nutzt spezifische Java-Typen gemäß Spezifikation:

-   IDs: `Integer` (nicht Long)
-   Preise/Kosten: `Float` (nicht Double oder BigDecimal)
-   Nährwerte: `Float`
-   Lagerbestände/Mengen: `Integer`

**Wichtig**: Bei Aggregationen (z.B. Dashboard-Summen) `Float` zu `Double` konvertieren, um Präzisionsverlust zu vermeiden.

## Sprache & Dokumentation

-   **Code-Kommentare**: Deutsch (z.B. "Gericht zum Speiseplan hinzufügen")
-   **Fehlermeldungen**: Deutsch (z.B. "Erforderlicher Parameter fehlt")
-   **Variablen-/Methodennamen**: Englisch (z.B. `getMealPlansForDateRange`)
-   **Commit-Messages**: Deutsch
-   **Dokumentation**: Deutsch

## Testing

-   Backend-Tests in `src/test/java/com/mymensa2/backend/`
-   Frontend-Tests nutzen React Testing Library (`@testing-library/react`)
-   Backend-Tests ausführen: `./mvnw test`
-   Frontend-Tests ausführen: `npm test`

## Erweiterte Anforderungen (Version 2)

### Mobile Apps

-   Studierende/Mitarbeitende: Mobile App für Vorbestellung
-   Mensa-Mitarbeiter: Separate Mobile App für QR-Code-Validierung bei Essensausgabe

### Zahlungsintegration

-   Integration mit EASYPAY für Kredit-/Debitkarten, Guthabenkonto, Bitcoin
-   Keine Barzahlung
-   QR-Code erst nach erfolgreicher Bezahlung abrufbar

### Lagerverwaltung

-   Verwaltung von Lagerbeständen und Zutaten
-   Automatische Nachbestellung via FOODSUPPLY bei Unterschreitung von Mindestmengen
-   Automatische Bestandsaktualisierung bei Zubereitung/Verkauf von Gerichten

### Prognosen

-   Wareneinsatz-Prognosen basierend auf vergangenen Bestellungen
-   Reduktion von Lebensmittelverschwendung und Förderung von Nachhaltigkeit

### Personalverwaltung

-   Erfassung von Arbeitszeiten der Köche und Servicekräfte
-   Synchronisation mit STAFFMAN für Verfügbarkeiten und Einsatzplanung
-   Planung basierend auf erwarteter Besucherzahl und geplanten Gerichten

### Gerichte-Kategorisierung

-   Kategorien: vegetarisch, vegan, halal, glutenfrei
-   Nährwertinformationen und Allergene pro Gericht

### Berichte

-   Automatisierte Erstellung von Einnahmen- und Kostenberichten
-   Einsehbar durch Verwaltung im Portal

## Coding-Standards

-   Nutze deutsche Kommentare für bessere Verständlichkeit im Team
-   Folge Clean Code Principles: kleine Methoden, klare Namen, Single Responsibility
-   Nutze `@Transactional` für alle schreibenden Service-Methoden
-   DTOs für alle Controller-Requests/-Responses (niemals Entities direkt zurückgeben)
-   Validierung mit Bean Validation (`@Valid`, `@NotNull`, etc.)
-   Fehlerbehandlung über Custom Exceptions und GlobalExceptionHandler

## Best Practices

-   **Schrittweise Implementierung**: Teile große Aufgaben in kleine, überschaubare Schritte
-   **Testen**: Schreibe Unit-Tests für Services, Integration-Tests für Controller
-   **Sicherheit**: Validiere alle Eingaben, nutze Prepared Statements (JPA macht das automatisch)
-   **Performance**: Nutze `@Transactional(readOnly = true)` für lesende Operationen
-   **Dokumentation**: Halte API-Dokumentation aktuell (z.B. in API-Endpoints.md)

# Backend API Endpoints - MyMensa2 (Version 2)

## 📋 Übersicht

MyMensa2 ist ein umfassendes Mensaverwaltungssystem mit folgenden Hauptfunktionen:

-   **Mobile Apps** für Studierende/Mitarbeitende (Vorbestellung) und Mensa-Mitarbeiter (QR-Code-Validierung)
-   **Zahlungsintegration** mit EASYPAY (Kredit-/Debitkarten, Guthabenkonto, Bitcoin)
-   **Lagerverwaltung** mit automatischer Nachbestellung via FOODSUPPLY
-   **Prognosen** zur Reduktion von Lebensmittelverschwendung
-   **Personalverwaltung** mit STAFFMAN-Synchronisation
-   **Verwaltungsportal** für Speisepläne, Berichte und Einstellungen

---

## 📊 Datenmodelle

### Meal-Objekt

```json
{
    "id": 1,
    "name": "Spaghetti Bolognese",
    "description": "Italienische Pasta mit Hackfleischsauce",
    "price": 6.5,
    "cost": 3.2,
    "ingredients": "Nudeln, Hackfleisch, Tomatensauce, Zwiebeln",
    "nutritionalInfo": {
        "calories": 650,
        "protein": 28.5,
        "carbs": 75.0,
        "fat": 18.3
    },
    "categories": ["Vegetarisch", "Glutenfrei"],
    "allergens": ["Gluten", "Milch/Laktose"],
    "deleted": false,
    "deletedAt": null
}
```

#### Datentypen & Beschreibung

| Feld              | Typ        | Pflicht   | Beschreibung                                      |
| ----------------- | ---------- | --------- | ------------------------------------------------- |
| `id`              | `int`      | Ja (auto) | Eindeutige Gericht-ID (nur bei Backend Responses) |
| `name`            | `string`   | Ja        | Name des Gerichts                                 |
| `description`     | `string`   | Ja        | Beschreibung des Gerichts                         |
| `price`           | `float`    | Ja        | Verkaufspreis in Euro                             |
| `cost`            | `float`    | Ja        | Produktionskosten in Euro                         |
| `ingredients`     | `string`   | Ja        | Komma-separierte Liste der Zutaten                |
| `nutritionalInfo` | `object`   | Ja        | Nährwertinformationen (siehe unten)               |
| `categories`      | `string[]` | Ja        | Array von Kategorien (kann leer sein)             |
| `allergens`       | `string[]` | Ja        | Array von Allergenen (kann leer sein)             |
| `deleted`         | `boolean`  | Ja        | Soft-Delete-Flag (false = aktiv)                  |
| `deletedAt`       | `datetime` | Nein      | Zeitpunkt der Löschung (null = nicht gelöscht)    |

---

### NutritionalInfo-Objekt

| Feld       | Typ     | Pflicht | Beschreibung           |
| ---------- | ------- | ------- | ---------------------- |
| `calories` | `int`   | Ja      | Kalorien in kcal       |
| `protein`  | `float` | Ja      | Protein in Gramm       |
| `carbs`    | `float` | Ja      | Kohlenhydrate in Gramm |
| `fat`      | `float` | Ja      | Fett in Gramm          |

---

### Ingredient-Objekt (NEU)

```json
{
    "id": 1,
    "name": "Tomaten",
    "unit": "kg",
    "stockQuantity": 50.0,
    "minStockLevel": 10.0,
    "pricePerUnit": 2.5,
    "supplierId": "FOODSUPPLY-VENDOR-123"
}
```

#### Datentypen & Beschreibung

| Feld            | Typ      | Pflicht   | Beschreibung                                   |
| --------------- | -------- | --------- | ---------------------------------------------- |
| `id`            | `int`    | Ja (auto) | Eindeutige Zutaten-ID                          |
| `name`          | `string` | Ja        | Name der Zutat                                 |
| `unit`          | `string` | Ja        | Mengeneinheit (kg, Liter, Stück, etc.)         |
| `stockQuantity` | `float`  | Ja        | Aktueller Lagerbestand                         |
| `minStockLevel` | `float`  | Ja        | Mindestbestand für automatische Nachbestellung |
| `pricePerUnit`  | `float`  | Ja        | Preis pro Einheit in Euro                      |
| `supplierId`    | `string` | Ja        | Lieferanten-ID in FOODSUPPLY-System            |

---

### Order-Objekt

```json
{
    "id": 1,
    "mealId": 1,
    "orderDate": "2025-01-12T14:30:00",
    "pickupDate": "2025-01-13",
    "paid": true,
    "paidAt": "2025-01-12T14:35:00",
    "paymentMethod": "CREDIT_CARD",
    "paymentTransactionId": "EASYPAY-TXN-789456",
    "qrCode": "ORDER-1",
    "collected": false,
    "collectedAt": null
}
```

#### Datentypen & Beschreibung

| Feld                   | Typ        | Pflicht   | Beschreibung                                                        |
| ---------------------- | ---------- | --------- | ------------------------------------------------------------------- |
| `id`                   | `int`      | Ja (auto) | Eindeutige Bestellungs-ID                                           |
| `mealId`               | `int`      | Ja        | Referenz zum bestellten Gericht                                     |
| `orderDate`            | `datetime` | Ja (auto) | Zeitpunkt der Bestellung                                            |
| `pickupDate`           | `date`     | Ja        | Geplantes Abholdatum                                                |
| `paid`                 | `boolean`  | Ja        | Zahlungsstatus                                                      |
| `paidAt`               | `datetime` | Nein      | Zeitpunkt der Bezahlung                                             |
| `paymentMethod`        | `string`   | Nein      | Zahlungsmethode (CREDIT_CARD, DEBIT_CARD, PREPAID_ACCOUNT, BITCOIN) |
| `paymentTransactionId` | `string`   | Nein      | EASYPAY-Transaktions-ID                                             |
| `qrCode`               | `string`   | Nein      | QR-Code für Essensausgabe (erst nach Bezahlung)                     |
| `collected`            | `boolean`  | Ja        | Abholstatus                                                         |
| `collectedAt`          | `datetime` | Nein      | Zeitpunkt der Abholung                                              |

---

### Staff-Objekt (NEU)

```json
{
    "id": 1,
    "firstName": "Anna",
    "lastName": "Schmidt",
    "role": "COOK",
    "staffmanId": "STAFFMAN-EMP-456",
    "isAvailable": true
}
```

#### Datentypen & Beschreibung

| Feld          | Typ       | Pflicht   | Beschreibung                                       |
| ------------- | --------- | --------- | -------------------------------------------------- |
| `id`          | `int`     | Ja (auto) | Eindeutige Mitarbeiter-ID                          |
| `firstName`   | `string`  | Ja        | Vorname                                            |
| `lastName`    | `string`  | Ja        | Nachname                                           |
| `role`        | `string`  | Ja        | Rolle (COOK, SERVICE, MANAGER)                     |
| `staffmanId`  | `string`  | Ja        | Mitarbeiter-ID in STAFFMAN-System                  |
| `isAvailable` | `boolean` | Ja        | Verfügbarkeitsstatus (aus STAFFMAN synchronisiert) |

---

### WorkingHours-Objekt (NEU)

```json
{
    "id": 1,
    "staffId": 1,
    "date": "2025-01-13",
    "startTime": "08:00:00",
    "endTime": "16:00:00",
    "hoursWorked": 8.0,
    "syncedToStaffman": true
}
```

#### Datentypen & Beschreibung

| Feld               | Typ       | Pflicht   | Beschreibung                        |
| ------------------ | --------- | --------- | ----------------------------------- |
| `id`               | `int`     | Ja (auto) | Eindeutige Arbeitszeit-ID           |
| `staffId`          | `int`     | Ja        | Referenz zum Mitarbeiter            |
| `date`             | `date`    | Ja        | Arbeitsdatum                        |
| `startTime`        | `time`    | Ja        | Arbeitsbeginn                       |
| `endTime`          | `time`    | Ja        | Arbeitsende                         |
| `hoursWorked`      | `float`   | Ja (auto) | Berechnete Arbeitsstunden           |
| `syncedToStaffman` | `boolean` | Ja        | Synchronisationsstatus mit STAFFMAN |

---

## ⚠️ Wichtige Hinweise

1. **Soft Delete**: Gerichte werden nicht hart gelöscht, sondern mit `deleted=true` markiert
2. **Zahlungsintegration**: Alle Zahlungen laufen über EASYPAY (keine Barzahlung)
3. **QR-Code**: Wird erst nach erfolgreicher Bezahlung generiert
4. **Externe Systeme**:
    - **EASYPAY**: Zahlungsabwicklung
    - **FOODSUPPLY**: Automatische Nachbestellung bei Lagerbeständen
    - **STAFFMAN**: Personalverwaltung und Einsatzplanung
5. **Mobile Apps**: Separate Apps für Kunden (Bestellung) und Mensa-Mitarbeiter (QR-Validierung)

---

## 🔗 API Endpunkte

### 📦 Gerichte-Verwaltung

#### 1. Alle aktiven Gerichte abrufen

```http
GET /api/meals
```

Lädt alle aktiven (nicht gelöschten) Gerichte aus der Datenbank.

**Request:** Keine Parameter erforderlich

**Response:** `200 OK`

```json
[
  { Meal-Objekt },
  { Meal-Objekt },
  ...
]
```

---

#### 2. Neues Gericht erstellen

```http
POST /api/meals
```

Erstellt ein neues Gericht in der Datenbank.

**Request Headers:**

```
Content-Type: application/json
```

**Request Body:**

```json
{
    "name": "Spaghetti Bolognese",
    "description": "Italienische Pasta mit Hackfleischsauce",
    "price": 6.5,
    "cost": 3.2,
    "ingredients": "Nudeln, Hackfleisch, Tomatensauce, Zwiebeln",
    "nutritionalInfo": {
        "calories": 650,
        "protein": 28.5,
        "carbs": 75.0,
        "fat": 18.3
    },
    "categories": ["Vegetarisch"],
    "allergens": ["Gluten", "Milch/Laktose"]
}
```

**Response:** `201 Created`

```json
{ Meal-Objekt mit generierter ID }
```

**Fehler:**

-   `400 Bad Request` - Ungültige Eingabedaten

---

#### 3. Gericht aktualisieren

```http
PUT /api/meals/{id}
```

Aktualisiert ein bestehendes Gericht.

**URL Parameter:**

-   `id` (erforderlich) - Die ID des zu aktualisierenden Gerichts

**Request Headers:**

```
Content-Type: application/json
```

**Request Body:**

```json
{ Meal-Objekt ohne id }
```

**Response:** `200 OK`

```json
{ Meal-Objekt mit aktualisierter ID }
```

**Fehler:**

-   `400 Bad Request` - Ungültige Eingabedaten
-   `404 Not Found` - Gericht mit angegebener ID existiert nicht

---

#### 4. Gericht löschen (Soft Delete)

```http
DELETE /api/meals/{id}
```

Markiert ein Gericht als gelöscht (Soft Delete), ohne es physisch aus der Datenbank zu entfernen. Dies ermöglicht die Erhaltung historischer Speisepläne und Bestellungen.

**URL Parameter:**

-   `id` (erforderlich) - Die ID des zu löschenden Gerichts

**Response:** `204 No Content`

**Hinweis:** Das Gericht wird mit `deleted=true` und `deletedAt=<aktueller Zeitstempel>` markiert

**Fehler:**

-   `404 Not Found` - Gericht mit angegebener ID existiert nicht

---

### 📅 Speiseplan-Verwaltung

#### 5. Speiseplan für einen Zeitraum abrufen

```http
GET /api/meal-plans?startDate={date}&endDate={date}
```

Lädt den Speiseplan für einen bestimmten Zeitraum (z.B. Montag bis Freitag).

**Query Parameter:**

-   `startDate` (erforderlich) - Startdatum im Format `YYYY-MM-DD` (z.B. `2025-01-13`)
-   `endDate` (erforderlich) - Enddatum im Format `YYYY-MM-DD` (z.B. `2025-01-17`)

**Request:**

```
GET /api/meal-plans?startDate=2025-01-13&endDate=2025-01-17
```

**Response:** `200 OK`

```json
[
  {
    "date": "2025-01-13",
    "meals": [
      {
        "meal": { Meal-Objekt },
        "stock": 50
      },
      {
        "meal": { Meal-Objekt },
        "stock": 30
      }
    ]
  },
  {
    "date": "2025-01-14",
    "meals": [
      {
        "meal": { Meal-Objekt },
        "stock": 25
      }
    ]
  }
]
```

**Response-Struktur:**

-   Array mit Objekten für jeden Tag im angegebenen Zeitraum
-   Jedes Objekt enthält:
    -   `date` (string) - Das Datum im Format `YYYY-MM-DD`
    -   `meals` (array) - Array von Objekten mit:
        -   `meal` (object) - Meal-Objekt mit allen Gerichtsinformationen
        -   `stock` (int) - Verfügbare Portionen für diesen Tag

**Fehler:**

-   `400 Bad Request` - Ungültiges Datumsformat oder endDate liegt vor startDate

---

#### 6. Gericht zum Speiseplan hinzufügen oder Bestand aktualisieren

```http
PUT /api/meal-plans
```

Fügt ein Gericht zu einem bestimmten Tag im Speiseplan hinzu oder aktualisiert den Bestand, falls es bereits existiert.

**Request Headers:**

```
Content-Type: application/json
```

**Request Body:**

```json
{
    "mealId": 1,
    "date": "2025-01-13",
    "stock": 50
}
```

**Response:** `200 OK` (bei Update) oder `201 Created` (bei Erstellung)

```json
{
  "mealId": 1,
  "date": "2025-01-13",
  "stock": 50,
  "meal": { Meal-Objekt }
}
```

**Fehler:**

-   `404 Not Found` - Gericht existiert nicht
-   `400 Bad Request` - Ungültige Eingabedaten

---

#### 7. Gericht aus Speiseplan entfernen

```http
DELETE /api/meal-plans?mealId={mealId}&date={date}
```

Entfernt ein Gericht von einem bestimmten Tag aus dem Speiseplan.

**Query Parameter:**

-   `mealId` (erforderlich) - Die ID des Gerichts
-   `date` (erforderlich) - Das Datum im Format `YYYY-MM-DD`

**Response:** `204 No Content`

**Fehler:**

-   `404 Not Found` - Gericht nicht im Speiseplan für diesen Tag gefunden

---

### 🛒 Bestellungs-Verwaltung (Mobile App)

#### 8. Bestellung erstellen

```http
POST /api/orders
```

Erstellt eine neue Bestellung für einen Nutzer über die mobile App.

**Request Headers:**

```
Content-Type: application/json
```

**Request Body:**

```json
{
    "mealId": 1,
    "pickupDate": "2025-01-13"
}
```

**Response:** `201 Created`

```json
{
    "orderId": 1,
    "orderDate": "2025-01-12T14:30:00",
    "pickupDate": "2025-01-13",
    "paid": false,
    "collected": false
}
```

**Fehler:**

-   `400 Bad Request` - Ungültige Eingabedaten oder nicht genügend Bestand
-   `404 Not Found` - Gericht existiert nicht im Speiseplan für diesen Tag

---

#### 9. Bestellung bezahlen (EASYPAY-Integration)

```http
PUT /api/orders/{orderId}/pay
```

Markiert eine Bestellung als bezahlt über EASYPAY und generiert einen QR-Code für die Essensausgabe.

**URL Parameter:**

-   `orderId` (erforderlich) - Die ID der zu bezahlenden Bestellung

**Request Headers:**

```
Content-Type: application/json
```

**Request Body:**

```json
{
    "paymentMethod": "CREDIT_CARD",
    "paymentTransactionId": "EASYPAY-TXN-789456"
}
```

**Verfügbare Zahlungsmethoden:**

-   `CREDIT_CARD` - Kreditkarte
-   `DEBIT_CARD` - Debitkarte
-   `PREPAID_ACCOUNT` - Guthabenkonto
-   `BITCOIN` - Bitcoin

**Response:** `200 OK`

```json
{
    "orderId": 1,
    "qrCode": "ORDER-1",
    "paidAt": "2025-01-12T14:35:00",
    "paymentMethod": "CREDIT_CARD",
    "paymentTransactionId": "EASYPAY-TXN-789456"
}
```

**Fehler:**

-   `404 Not Found` - Bestellung existiert nicht
-   `400 Bad Request` - Bestellung wurde bereits bezahlt oder ungültige Zahlungsmethode

---

#### 10. Bestellung per QR-Code validieren (Mensa-Mitarbeiter App)

```http
POST /api/orders/validate
```

Validiert eine Bestellung anhand des QR-Codes bei der Essensausgabe und markiert sie als abgeholt. Wird von der mobilen App für Mensa-Mitarbeiter verwendet.

**Request Headers:**

```
Content-Type: application/json
```

**Request Body:**

```json
{
    "qrCode": "ORDER-1"
}
```

**Response (Erste Abholung):** `200 OK`

```json
{
  "alreadyCollected": false,
  "collectedAt": "2025-01-13T12:15:00",
  "orderId": 1,
  "orderDate": "2025-01-12T14:30:00",
  "pickupDate": "2025-01-13",
  "meal": { Meal-Objekt }
}
```

**Response (Bereits abgeholt):** `200 OK`

```json
{
  "alreadyCollected": true,
  "collectedAt": "2025-01-13T12:15:00",
  "orderId": 1,
  "orderDate": "2025-01-12T14:30:00",
  "pickupDate": "2025-01-13",
  "meal": { Meal-Objekt }
}
```

**Fehler:**

-   `404 Not Found` - QR-Code ungültig oder Bestellung nicht bezahlt
-   `400 Bad Request` - QR-Code-Format ungültig

---

#### 11. Alle Bestellungen abrufen (Admin)

```http
GET /api/orders
```

Lädt alle Bestellungen für das Admin-Panel.

**Query Parameter (optional):**

-   `startDate` - Filtert nach Bestellungen ab diesem Datum (Format: `YYYY-MM-DD`)
-   `endDate` - Filtert nach Bestellungen bis zu diesem Datum (Format: `YYYY-MM-DD`)
-   `paid` - Filtert nach Zahlungsstatus (`true` oder `false`)
-   `collected` - Filtert nach Abholstatus (`true` oder `false`)

**Request:**

```
GET /api/orders?startDate=2025-01-10&endDate=2025-01-15&paid=true
```

**Response:** `200 OK`

```json
[
  {
    "id": 1,
    "meal": { Meal-Objekt },
    "orderDate": "2025-01-12T14:30:00",
    "pickupDate": "2025-01-13",
    "paid": true,
    "paidAt": "2025-01-12T14:35:00",
    "paymentMethod": "CREDIT_CARD",
    "collected": true,
    "collectedAt": "2025-01-13T12:15:00"
  }
]
```

---

### 📈 Dashboard & Berichte

#### 12. Dashboard-Daten abrufen

```http
GET /api/dashboard
```

Lädt Finanz- und Verkaufsdaten für das Verwaltungsportal.

**Query Parameter (optional):**

-   `startDate` - Analysezeitraum Start (Format: `YYYY-MM-DD`)
-   `endDate` - Analysezeitraum Ende (Format: `YYYY-MM-DD`)

**Request:**

```
GET /api/dashboard?startDate=2025-01-01&endDate=2025-01-31
```

**Response:** `200 OK`

```json
{
    "totalRevenue": 1960.9,
    "totalExpenses": 750.7,
    "profit": 1210.2,
    "mealStats": [
        {
            "mealName": "Spaghetti Bolognese",
            "quantitySold": 90,
            "totalRevenue": 585.0,
            "totalExpenses": 288.0
        },
        {
            "mealName": "Veganer Burger",
            "quantitySold": 89,
            "totalRevenue": 703.1,
            "totalExpenses": 400.5
        }
    ]
}
```

**Response-Struktur:**

-   `totalRevenue` (double) - Gesamteinnahmen aus allen verkauften Gerichten
-   `totalExpenses` (double) - Gesamtausgaben für alle verkauften Gerichte (Wareneinsatz)
-   `profit` (double) - Gewinn (totalRevenue - totalExpenses)
-   `mealStats` (array) - Array von Meal-Statistik-Objekten mit:
    -   `mealName` (string) - Name des Gerichts
    -   `quantitySold` (int) - Anzahl verkaufter Portionen
    -   `totalRevenue` (double) - Gesamteinnahmen für dieses Gericht
    -   `totalExpenses` (double) - Gesamtausgaben für dieses Gericht

---

### 📦 Lagerverwaltung (NEU)

#### 13. Alle Zutaten abrufen

```http
GET /api/inventory
```

Lädt alle Zutaten aus dem Lager.

**Query Parameter (optional):**

-   `lowStock` - Wenn `true`, nur Zutaten unter Mindestbestand (`boolean`)

**Request:**

```
GET /api/inventory?lowStock=true
```

**Response:** `200 OK`

```json
[
    {
        "id": 1,
        "name": "Tomaten",
        "unit": "kg",
        "stockQuantity": 8.5,
        "minStockLevel": 10.0,
        "pricePerUnit": 2.5,
        "supplierId": "FOODSUPPLY-VENDOR-123",
        "needsReorder": true
    },
    {
        "id": 2,
        "name": "Nudeln",
        "unit": "kg",
        "stockQuantity": 50.0,
        "minStockLevel": 20.0,
        "pricePerUnit": 1.8,
        "supplierId": "FOODSUPPLY-VENDOR-456",
        "needsReorder": false
    }
]
```

---

#### 14. Zutat erstellen

```http
POST /api/inventory
```

Erstellt eine neue Zutat im Lager.

**Request Headers:**

```
Content-Type: application/json
```

**Request Body:**

```json
{
    "name": "Tomaten",
    "unit": "kg",
    "stockQuantity": 50.0,
    "minStockLevel": 10.0,
    "pricePerUnit": 2.5,
    "supplierId": "FOODSUPPLY-VENDOR-123"
}
```

**Response:** `201 Created`

```json
{ Ingredient-Objekt mit generierter ID }
```

**Fehler:**

-   `400 Bad Request` - Ungültige Eingabedaten

---

#### 15. Zutat aktualisieren

```http
PUT /api/inventory/{id}
```

Aktualisiert eine bestehende Zutat (z.B. Lagerbestand nach Lieferung).

**URL Parameter:**

-   `id` (erforderlich) - Die ID der Zutat

**Request Headers:**

```
Content-Type: application/json
```

**Request Body:**

```json
{
    "stockQuantity": 80.0
}
```

**Response:** `200 OK`

```json
{ Ingredient-Objekt mit aktualisiertem Bestand }
```

**Fehler:**

-   `404 Not Found` - Zutat existiert nicht
-   `400 Bad Request` - Ungültige Eingabedaten

---

#### 16. Automatische Nachbestellung auslösen (FOODSUPPLY-Integration)

```http
POST /api/inventory/reorder
```

Löst eine automatische Nachbestellung aller Zutaten unter Mindestbestand über FOODSUPPLY aus.

**Request:** Keine Parameter erforderlich

**Response:** `200 OK`

```json
{
    "reorderedItems": [
        {
            "ingredientId": 1,
            "ingredientName": "Tomaten",
            "currentStock": 8.5,
            "minStockLevel": 10.0,
            "reorderQuantity": 50.0,
            "supplierId": "FOODSUPPLY-VENDOR-123",
            "foodsupplyOrderId": "FOODSUPPLY-ORD-789"
        }
    ],
    "totalOrderValue": 125.0
}
```

**Fehler:**

-   `500 Internal Server Error` - Verbindung zu FOODSUPPLY fehlgeschlagen

---

#### 17. Lagerbestand nach Gerichtszubereitung aktualisieren

```http
PUT /api/inventory/consume
```

Aktualisiert den Lagerbestand automatisch nach Zubereitung/Verkauf von Gerichten.

**Request Headers:**

```
Content-Type: application/json
```

**Request Body:**

```json
{
    "mealId": 1,
    "quantity": 10
}
```

**Response:** `200 OK`

```json
{
    "mealName": "Spaghetti Bolognese",
    "quantityPrepared": 10,
    "ingredientsConsumed": [
        {
            "ingredientName": "Nudeln",
            "quantityUsed": 2.5,
            "unit": "kg",
            "remainingStock": 47.5
        },
        {
            "ingredientName": "Tomaten",
            "quantityUsed": 5.0,
            "unit": "kg",
            "remainingStock": 3.5,
            "needsReorder": true
        }
    ]
}
```

**Fehler:**

-   `404 Not Found` - Gericht oder Zutat existiert nicht
-   `400 Bad Request` - Nicht genügend Bestand

---

### 👥 Personalverwaltung (NEU)

#### 18. Alle Mitarbeiter abrufen

```http
GET /api/staff
```

Lädt alle Mensa-Mitarbeiter.

**Query Parameter (optional):**

-   `role` - Filtert nach Rolle (`COOK`, `SERVICE`, `MANAGER`)
-   `available` - Filtert nach Verfügbarkeit (`boolean`)

**Request:**

```
GET /api/staff?role=COOK&available=true
```

**Response:** `200 OK`

```json
[
    {
        "id": 1,
        "firstName": "Anna",
        "lastName": "Schmidt",
        "role": "COOK",
        "staffmanId": "STAFFMAN-EMP-456",
        "isAvailable": true
    }
]
```

---

#### 19. Mitarbeiter erstellen

```http
POST /api/staff
```

Erstellt einen neuen Mitarbeiter und synchronisiert mit STAFFMAN.

**Request Headers:**

```
Content-Type: application/json
```

**Request Body:**

```json
{
    "firstName": "Anna",
    "lastName": "Schmidt",
    "role": "COOK"
}
```

**Response:** `201 Created`

```json
{
    "id": 1,
    "firstName": "Anna",
    "lastName": "Schmidt",
    "role": "COOK",
    "staffmanId": "STAFFMAN-EMP-456",
    "isAvailable": true
}
```

**Fehler:**

-   `400 Bad Request` - Ungültige Eingabedaten
-   `500 Internal Server Error` - STAFFMAN-Synchronisation fehlgeschlagen

---

#### 20. Arbeitszeiten erfassen

```http
POST /api/staff/{staffId}/working-hours
```

Erfasst Arbeitszeiten eines Mitarbeiters und synchronisiert mit STAFFMAN.

**URL Parameter:**

-   `staffId` (erforderlich) - Die ID des Mitarbeiters

**Request Headers:**

```
Content-Type: application/json
```

**Request Body:**

```json
{
    "date": "2025-01-13",
    "startTime": "08:00:00",
    "endTime": "16:00:00"
}
```

**Response:** `201 Created`

```json
{
    "id": 1,
    "staffId": 1,
    "date": "2025-01-13",
    "startTime": "08:00:00",
    "endTime": "16:00:00",
    "hoursWorked": 8.0,
    "syncedToStaffman": true
}
```

**Fehler:**

-   `404 Not Found` - Mitarbeiter existiert nicht
-   `400 Bad Request` - Ungültige Zeitangaben
-   `500 Internal Server Error` - STAFFMAN-Synchronisation fehlgeschlagen

---

#### 21. Arbeitszeiten für Zeitraum abrufen

```http
GET /api/staff/working-hours?startDate={date}&endDate={date}
```

Lädt Arbeitszeiten aller Mitarbeiter für einen bestimmten Zeitraum.

**Query Parameter:**

-   `startDate` (erforderlich) - Startdatum im Format `YYYY-MM-DD`
-   `endDate` (erforderlich) - Enddatum im Format `YYYY-MM-DD`
-   `staffId` (optional) - Filtert nach Mitarbeiter-ID

**Request:**

```
GET /api/staff/working-hours?startDate=2025-01-10&endDate=2025-01-15&staffId=1
```

**Response:** `200 OK`

```json
[
  {
    "id": 1,
    "staff": { Staff-Objekt },
    "date": "2025-01-13",
    "startTime": "08:00:00",
    "endTime": "16:00:00",
    "hoursWorked": 8.0,
    "syncedToStaffman": true
  }
]
```

---

#### 22. Einsatzplanung basierend auf erwarteter Besucherzahl

```http
GET /api/staff/schedule-recommendation?date={date}
```

Gibt eine Empfehlung für die Mitarbeitereinsatzplanung basierend auf der erwarteten Besucherzahl und geplanten Gerichten.

**Query Parameter:**

-   `date` (erforderlich) - Datum im Format `YYYY-MM-DD`

**Request:**

```
GET /api/staff/schedule-recommendation?date=2025-01-13
```

**Response:** `200 OK`

```json
{
  "date": "2025-01-13",
  "expectedVisitors": 350,
  "plannedMeals": 5,
  "recommendedStaff": {
    "cooks": 3,
    "service": 4,
    "total": 7
  },
  "availableStaff": {
    "cooks": [
      { Staff-Objekt },
      { Staff-Objekt }
    ],
    "service": [
      { Staff-Objekt }
    ]
  }
}
```

---

### 📊 Prognosen & Nachhaltigkeit (NEU)

#### 23. Wareneinsatz-Prognose für Zeitraum

```http
GET /api/forecasts/demand?startDate={date}&endDate={date}
```

Erstellt eine Prognose für den Wareneinsatz basierend auf vergangenen Bestellungen.

**Query Parameter:**

-   `startDate` (erforderlich) - Startdatum im Format `YYYY-MM-DD`
-   `endDate` (erforderlich) - Enddatum im Format `YYYY-MM-DD`

**Request:**

```
GET /api/forecasts/demand?startDate=2025-01-20&endDate=2025-01-24
```

**Response:** `200 OK`

```json
{
    "forecastPeriod": {
        "startDate": "2025-01-20",
        "endDate": "2025-01-24"
    },
    "mealForecasts": [
        {
            "mealName": "Spaghetti Bolognese",
            "averageDailyDemand": 18.5,
            "recommendedStock": 95,
            "confidenceLevel": 0.85
        },
        {
            "mealName": "Veganer Burger",
            "averageDailyDemand": 15.2,
            "recommendedStock": 80,
            "confidenceLevel": 0.78
        }
    ],
    "ingredientForecasts": [
        {
            "ingredientName": "Tomaten",
            "estimatedConsumption": 45.5,
            "currentStock": 50.0,
            "recommendedPurchase": 0.0
        },
        {
            "ingredientName": "Nudeln",
            "estimatedConsumption": 28.0,
            "currentStock": 15.0,
            "recommendedPurchase": 20.0
        }
    ]
}
```

---

#### 24. Nachhaltigkeit-Bericht

```http
GET /api/reports/sustainability?month={month}&year={year}
```

Erstellt einen Bericht über Lebensmittelverschwendung und Nachhaltigkeit.

**Query Parameter:**

-   `month` (erforderlich) - Monat (1-12)
-   `year` (erforderlich) - Jahr (z.B. 2025)

**Request:**

```
GET /api/reports/sustainability?month=1&year=2025
```

**Response:** `200 OK`

```json
{
    "period": "Januar 2025",
    "wasteReduction": {
        "totalMealsPrepared": 2450,
        "totalMealsSold": 2380,
        "wastedMeals": 70,
        "wastePercentage": 2.86,
        "previousMonthWastePercentage": 5.2,
        "improvement": 2.34
    },
    "costSavings": {
        "savedCosts": 224.0,
        "potentialSavings": 156.0
    },
    "topWastedMeals": [
        {
            "mealName": "Caesar Salad",
            "wastedPortions": 25,
            "costOfWaste": 75.0
        }
    ],
    "recommendations": [
        "Reduktion der Caesar Salad Portionen um 10%",
        "Erhöhung der Veganer Burger Produktion um 5%"
    ]
}
```

---

## 🔄 Externe System-Integrationen

### EASYPAY (Zahlungsdienstleister)

-   **Zweck**: Abwicklung aller Zahlungen
-   **Unterstützte Methoden**: Kreditkarte, Debitkarte, Guthabenkonto, Bitcoin
-   **Integration**: Bei Bestellbezahlung wird Transaktions-ID von EASYPAY gespeichert
-   **Keine Barzahlung möglich**

### FOODSUPPLY (Lieferanten-System)

-   **Zweck**: Automatische Nachbestellung bei niedrigen Lagerbeständen
-   **Trigger**: Wenn `stockQuantity < minStockLevel`
-   **Integration**: API-Aufruf an FOODSUPPLY mit Lieferanten-ID
-   **Response**: FOODSUPPLY-Order-ID zur Nachverfolgung

### STAFFMAN (Personalverwaltung)

-   **Zweck**: Synchronisation von Mitarbeiterverfügbarkeiten und Arbeitszeiten
-   **Bidirektionale Synchronisation**:
    -   MYMENSA → STAFFMAN: Erfasste Arbeitszeiten
    -   STAFFMAN → MYMENSA: Verfügbarkeiten und Urlaubszeiten
-   **Integration**: REST-API mit periodischer Synchronisation

---

## 🔐 Sicherheitshinweise

-   **Aktuell keine Authentifizierung** - Security-Aspekte werden in zukünftigen Versionen implementiert
-   **Kein Login/Logout** - Offenes System für Entwicklungsphase
-   **Validierung**: Alle Eingaben werden serverseitig validiert
-   **Externe Systeme**: Sichere API-Keys für EASYPAY, FOODSUPPLY, STAFFMAN erforderlich

---

## 📱 Mobile Apps

### Kunden-App (Studierende/Mitarbeitende)

-   Speiseplan-Ansicht
-   Bestellung aufgeben
-   Zahlung über EASYPAY
-   QR-Code für Essensausgabe anzeigen
-   Bestellhistorie

### Mitarbeiter-App (Mensa-Personal)

-   QR-Code-Scanner für Essensausgabe
-   Validierung von Bestellungen
-   Übersicht über aktuelle Bestellungen
-   Lagerbestand-Übersicht

### Verwaltungsportal (Web)

-   Speiseplan-Verwaltung
-   Gerichte-Verwaltung
-   Lagerverwaltung
-   Personalverwaltung
-   Dashboard mit Berichten
-   Prognosen und Nachhaltigkeit

---

## 🚀 Weitere geplante Features

-   **Allergen-Warnungen** in Kunden-App basierend auf Nutzerprofil
-   **Push-Benachrichtigungen** für bereite Bestellungen
-   **Treueprogramm** mit Punktesammlung
-   **Bewertungen & Feedback** für Gerichte
-   **KI-basierte Prognosen** mit Machine Learning
-   **Multi-Sprachen-Support** (DE, EN)
