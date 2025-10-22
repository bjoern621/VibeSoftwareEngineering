
# MyMensa2 – KI-Coding-Assistent Anweisungen

## Projektübersicht

MyMensa2 ist ein digitales Mensaverwaltungssystem mit Spring Boot Backend und React Frontend. Das System verwaltet Gerichte, Speisepläne, Bestellungen mit QR-Codes, Lagerverwaltung, Zahlungsintegration (EASYPAY) und bietet Finanz-Dashboards sowie Prognosen zur Reduktion von Lebensmittelverschwendung.

### Systemkontext

**MyMensa2** interagiert mit folgenden externen Systemen:
- **EASYPAY**: Bezahldienstleister für Zahlungen via Kredit-/Debitkarte, Guthabenkonto, Bitcoin (keine Barzahlung)
- **FOODSUPPLY**: Automatische Nachbestellung bei Lieferanten bei Unterschreitung von Mindestbeständen
- **STAFFMAN**: Synchronisation von Arbeitszeiten der Mitarbeitenden und Einsatzplanung basierend auf Besucherzahlen

**Nutzergruppen**:
- Studierende/Mitarbeitende: Vorbestellung via Mobile App, Abholung mit QR-Code nach Bezahlung
- Mensa-Mitarbeitende: QR-Code-Validierung bei Essensausgabe über separate Mobile App
- Mensaverwaltung: Portal für Speiseplan, Lagerbestände, Berichte, Gerichte- und Personalverwaltung

## Tech Stack

### Backend
- **Framework**: Spring Boot 3.5.6
- **Java-Version**: Java 25
- **Datenbank**: H2 In-Memory
- **Persistierung**: Spring Data JPA, Hibernate
- **Build-Tool**: Maven

### Frontend
- **Framework**: React 19.2.0
- **Build-Tool**: Create React App (react-scripts 5.0.1)
- **Dev-Server**: Port 3001 (MyMensa2)
- **HTTP-Client**: Fetch API, Proxy zu Backend auf Port 8081

### Architektur
- **Backend**: Clean Architecture (3-Schichten-Modell)
- **Frontend**: Komponentenbasierte Architektur mit funktionalen React-Komponenten

## Architektur-Pattern: Clean Architecture (3-Schicht-Modell)

Das Backend folgt einem strikten **3-Schichten-Muster** mit klarer Trennung:

```
feature/
├── dataaccess/     # Entities (@Entity), Repositories (@Repository), Composite Keys
├── logic/          # Business-Services (@Service), @Transactional, Validierung
└── facade/         # REST-Controller (@RestController), DTOs (Java Records)
```

### Verantwortlichkeiten der Schichten
- **dataaccess**: JPA-Entities mit `@Entity`, Repositories mit `@Repository`, Composite Keys
- **logic**: Business-Services mit `@Service`, `@Transactional` für Transaktionen, Validierungslogik
- **facade**: REST-Controller mit `@RestController`, DTOs als Java Records, HTTP-Layer-Logik

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
- `ResourceNotFoundException` → 404 mit strukturiertem Fehler-JSON
- `InvalidRequestException` → 400 mit deutscher Fehlermeldung
- `DataIntegrityViolationException` → 409 mit nutzerfreundlicher Constraint-Meldung

**Wichtig**: Wirf immer Custom-Exceptions, statt direkt Error-Responses zurückzugeben.

### 4. Datumsverarbeitung
- Nutze `LocalDate` für reine Datumswerte (Speisepläne, Bestellungen)
- Nutze `LocalDateTime` für Zeitstempel (Bestellerstellung, Abholung)
- Frontend sendet Daten als `"yyyy-MM-dd"`-Strings, Backend parst mit `DateTimeFormatter.ofPattern("yyyy-MM-dd")`

### 5. DTO-Pattern mit Records
Nutze Java Records für DTOs (Java 17+):
```
public record MealPlanRequestDTO(Integer mealId, String date, Integer stock) {}
```

## Datenbank-Konfiguration

- **H2 In-Memory** für Entwicklung (siehe `application.properties`)
- **Schema-Erstellung**: `spring.jpa.hibernate.ddl-auto=create-drop`
- **Testdaten**: Laden aus `data.sql` nach Schema-Erstellung (`spring.jpa.defer-datasource-initialization=true`)
- **H2 Console**: `http://localhost:8080/h2-console` bzw. `http://localhost:8081/h2-console` (JDBC URL: `jdbc:h2:mem:mymensa` bzw. `jdbc:h2:mem:mymensa2`)

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

- **Meal** (1) → (*) **MealPlan**: Ein Gericht kann in mehreren Speiseplänen an verschiedenen Daten erscheinen
- **Meal** (1) → (*) **Order**: Ein Gericht kann mehrfach bestellt werden
- **Order** hat QR-Code zur Abhol-Verifizierung und Zahlungstracking (`paid`, `collected` Flags)
- **Dashboard** aggregiert Bestelldaten zur Berechnung von Einnahmen (price) vs. Ausgaben (cost)
- **Inventory**: Lagerverwaltung mit Mindestbeständen, automatische Nachbestellung via FOODSUPPLY
- **Ingredient**: Zutaten mit Bestandsverfolgung, Aktualisierung bei Zubereitung/Verkauf
- **Staff**: Arbeitszeiten der Mitarbeitenden, Synchronisation mit STAFFMAN

## Frontend-Struktur

React-App mit komponentenbasierter Architektur:
- **App.js**: Hauptnavigation zwischen OrderManagement und AdminPanel
- **OrderManagement**: Kundenoberfläche zur Bestellaufgabe
- **AdminPanel**: Admin-Hub mit Tabs für Dashboard, Meals, MealPlans, Orders, Inventory, Staff
- Jede Admin-Funktion ist separate Komponente (MealManagement.js, Dashboard.js, InventoryManagement.js, etc.)

## Typ-Präzision

Backend nutzt spezifische Java-Typen gemäß Spezifikation:
- IDs: `Integer` (nicht Long)
- Preise/Kosten: `Float` (nicht Double oder BigDecimal)
- Nährwerte: `Float`
- Lagerbestände/Mengen: `Integer`

**Wichtig**: Bei Aggregationen (z.B. Dashboard-Summen) `Float` zu `Double` konvertieren, um Präzisionsverlust zu vermeiden.

## Sprache & Dokumentation

- **Code-Kommentare**: Deutsch (z.B. "Gericht zum Speiseplan hinzufügen")
- **Fehlermeldungen**: Deutsch (z.B. "Erforderlicher Parameter fehlt")
- **Variablen-/Methodennamen**: Englisch (z.B. `getMealPlansForDateRange`)
- **Commit-Messages**: Deutsch
- **Dokumentation**: Deutsch

## Testing

- Backend-Tests in `src/test/java/com/mymensa2/backend/`
- Frontend-Tests nutzen React Testing Library (`@testing-library/react`)
- Backend-Tests ausführen: `./mvnw test`
- Frontend-Tests ausführen: `npm test`

## Erweiterte Anforderungen (Version 2)

### Mobile Apps
- Studierende/Mitarbeitende: Mobile App für Vorbestellung
- Mensa-Mitarbeiter: Separate Mobile App für QR-Code-Validierung bei Essensausgabe

### Zahlungsintegration
- Integration mit EASYPAY für Kredit-/Debitkarten, Guthabenkonto, Bitcoin
- Keine Barzahlung
- QR-Code erst nach erfolgreicher Bezahlung abrufbar

### Lagerverwaltung
- Verwaltung von Lagerbeständen und Zutaten
- Automatische Nachbestellung via FOODSUPPLY bei Unterschreitung von Mindestmengen
- Automatische Bestandsaktualisierung bei Zubereitung/Verkauf von Gerichten

### Prognosen
- Wareneinsatz-Prognosen basierend auf vergangenen Bestellungen
- Reduktion von Lebensmittelverschwendung und Förderung von Nachhaltigkeit

### Personalverwaltung
- Erfassung von Arbeitszeiten der Köche und Servicekräfte
- Synchronisation mit STAFFMAN für Verfügbarkeiten und Einsatzplanung
- Planung basierend auf erwarteter Besucherzahl und geplanten Gerichten

### Gerichte-Kategorisierung
- Kategorien: vegetarisch, vegan, halal, glutenfrei
- Nährwertinformationen und Allergene pro Gericht

### Berichte
- Automatisierte Erstellung von Einnahmen- und Kostenberichten
- Einsehbar durch Verwaltung im Portal

## Coding-Standards

- Nutze deutsche Kommentare für bessere Verständlichkeit im Team
- Folge Clean Code Principles: kleine Methoden, klare Namen, Single Responsibility
- Nutze `@Transactional` für alle schreibenden Service-Methoden
- DTOs für alle Controller-Requests/-Responses (niemals Entities direkt zurückgeben)
- Validierung mit Bean Validation (`@Valid`, `@NotNull`, etc.)
- Fehlerbehandlung über Custom Exceptions und GlobalExceptionHandler

## Best Practices

- **Schrittweise Implementierung**: Teile große Aufgaben in kleine, überschaubare Schritte
- **Testen**: Schreibe Unit-Tests für Services, Integration-Tests für Controller
- **Sicherheit**: Validiere alle Eingaben, nutze Prepared Statements (JPA macht das automatisch)
- **Performance**: Nutze `@Transactional(readOnly = true)` für lesende Operationen
- **Dokumentation**: Halte API-Dokumentation aktuell (z.B. in API-Endpoints.md)
