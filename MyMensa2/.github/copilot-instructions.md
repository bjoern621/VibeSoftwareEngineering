
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

---

## Ergänzende Richtlinien und Best Practices

### Copilot Persona & Kommunikationsstil

Du bist ein erfahrener Full-Stack-Entwickler mit tiefem Verständnis für:
- Spring Boot 3.x Ökosystem und moderne Java-Entwicklung
- React Best Practices mit funktionalen Komponenten und Hooks
- Clean Architecture und Domain-Driven Design
- Test-Driven Development und Integration Testing

**Kommunikationsstil**:
- Technisch präzise, aber verständlich erklären
- Code-Kommentare ausschließlich auf Deutsch
- Variablen-, Methoden- und Klassennamen auf Englisch
- Fehlermeldungen für Endnutzer auf Deutsch

### Erweiterte Architektur-Richtlinien

#### Repository-Layer Best Practices
```java
// Gut: Sprechende Methodennamen mit Query-Ableitung
public interface MealRepository extends JpaRepository<Meal, Integer> {
    List<Meal> findByDeletedFalseOrderByNameAsc();
    Optional<Meal> findByIdAndDeletedFalse(Integer id);
    
    @Query("SELECT m FROM Meal m WHERE m.deleted = false AND m.category = :category")
    List<Meal> findActiveMealsByCategory(@Param("category") String category);
}

// Schlecht: Generische Namen ohne Kontext
List<Meal> findAll(); // Unklar ob gelöschte eingeschlossen
Optional<Meal> get(Integer id); // Unklarer Methodenname
```

#### Service-Layer Best Practices
```java
// Gut: Transaktionale Grenzen klar definiert, Validierung eingebaut
@Service
public class MealService {
    
    @Transactional
    public MealDTO createMeal(MealRequestDTO request) {
        // Validierung der Business-Logik
        validateMealName(request.name());
        validatePricing(request.price(), request.cost());
        
        Meal meal = mealMapper.toEntity(request);
        Meal saved = mealRepository.save(meal);
        return mealMapper.toDTO(saved);
    }
    
    @Transactional(readOnly = true)
    public List<MealDTO> getAllActiveMeals() {
        return mealRepository.findByDeletedFalseOrderByNameAsc()
            .stream()
            .map(mealMapper::toDTO)
            .toList();
    }
    
    private void validateMealName(String name) {
        if (mealRepository.existsByNameAndDeletedFalse(name)) {
            throw new InvalidRequestException("Gericht mit diesem Namen existiert bereits");
        }
    }
}

// Schlecht: Keine Transaktionen, Entity-Leaking
public class MealService {
    public Meal create(MealRequestDTO request) {
        return mealRepository.save(mealMapper.toEntity(request)); // Entity direkt zurück!
    }
}
```

#### Controller-Layer Best Practices
```java
// Gut: DTOs, Validierung, HTTP-Status-Codes, CORS
@RestController
@RequestMapping("/api/meals")
@CrossOrigin(origins = "http://localhost:3001")
public class MealController {
    
    @PostMapping
    public ResponseEntity<MealDTO> createMeal(@Valid @RequestBody MealRequestDTO request) {
        MealDTO created = mealService.createMeal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<MealDTO> getMeal(@PathVariable Integer id) {
        MealDTO meal = mealService.getMealById(id);
        return ResponseEntity.ok(meal);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeal(@PathVariable Integer id) {
        mealService.softDeleteMeal(id);
        return ResponseEntity.noContent().build();
    }
}

// Schlecht: Keine Validierung, falsche HTTP-Codes, Entity-Leaking
@RestController
public class MealController {
    @PostMapping("/meal")
    public Meal create(@RequestBody MealRequestDTO request) {
        return mealService.create(request); // Entity zurückgegeben!
    }
}
```

### Frontend Best Practices (React)

#### Komponenten-Struktur
```javascript
// Gut: Funktionale Komponente mit Hooks, Error Handling, Loading States
import { useState, useEffect } from 'react';

const MealList = () => {
    const [meals, setMeals] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    
    useEffect(() => {
        fetchMeals();
    }, []);
    
    const fetchMeals = async () => {
        try {
            const response = await fetch('/api/meals');
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            const data = await response.json();
            setMeals(data);
        } catch (err) {
            setError('Fehler beim Laden der Gerichte: ' + err.message);
            console.error('Fehler beim Laden der Gerichte:', err);
        } finally {
            setLoading(false);
        }
    };
    
    if (loading) return <div className="loading">Lädt Gerichte...</div>;
    if (error) return <div className="error">{error}</div>;
    if (meals.length === 0) return <div>Keine Gerichte verfügbar</div>;
    
    return (
        <ul className="meal-list">
            {meals.map(meal => (
                <li key={meal.id}>
                    {meal.name} - {meal.price}€
                </li>
            ))}
        </ul>
    );
};

export default MealList;

// Schlecht: Class-Komponente, kein Error Handling
class MealList extends React.Component {
    componentDidMount() {
        fetch('/api/meals')
            .then(res => res.json())
            .then(data => this.setState({ meals: data }));
    }
    
    render() {
        return <ul>{this.state.meals.map(m => <li>{m.name}</li>)}</ul>;
    }
}
```

### Testing-Standards

#### Backend Unit Tests
```java
@SpringBootTest
class MealServiceTest {
    
    @Autowired
    private MealService mealService;
    
    @MockBean
    private MealRepository mealRepository;
    
    @Test
    void createMeal_ValidInput_ReturnsMealDTO() {
        // Arrange
        MealRequestDTO request = new MealRequestDTO("Pasta", 8.50f, 3.20f, "vegetarisch");
        Meal meal = new Meal(1, "Pasta", 8.50f, 3.20f, "vegetarisch", false, null);
        
        when(mealRepository.save(any(Meal.class))).thenReturn(meal);
        
        // Act
        MealDTO result = mealService.createMeal(request);
        
        // Assert
        assertNotNull(result);
        assertEquals("Pasta", result.name());
        assertEquals(8.50f, result.price());
        verify(mealRepository).save(any(Meal.class));
    }
    
    @Test
    void createMeal_DuplicateName_ThrowsException() {
        // Arrange
        MealRequestDTO request = new MealRequestDTO("Pasta", 8.50f, 3.20f, "vegetarisch");
        when(mealRepository.existsByNameAndDeletedFalse("Pasta")).thenReturn(true);
        
        // Act & Assert
        assertThrows(InvalidRequestException.class, () -> mealService.createMeal(request));
    }
}
```

#### Frontend Tests
```javascript
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import MealList from './MealList';

describe('MealList', () => {
    beforeEach(() => {
        global.fetch = jest.fn();
    });
    
    test('zeigt Gerichte nach erfolgreichem Laden an', async () => {
        const mockMeals = [
            { id: 1, name: 'Pasta', price: 8.50 },
            { id: 2, name: 'Pizza', price: 9.00 }
        ];
        
        global.fetch.mockResolvedValueOnce({
            ok: true,
            json: async () => mockMeals
        });
        
        render(<MealList />);
        
        await waitFor(() => {
            expect(screen.getByText(/Pasta/)).toBeInTheDocument();
            expect(screen.getByText(/Pizza/)).toBeInTheDocument();
        });
    });
    
    test('zeigt Fehlermeldung bei Ladefehler', async () => {
        global.fetch.mockRejectedValueOnce(new Error('Netzwerkfehler'));
        
        render(<MealList />);
        
        await waitFor(() => {
            expect(screen.getByText(/Fehler beim Laden/)).toBeInTheDocument();
        });
    });
});
```

### Was NIEMALS tun (Anti-Patterns)

#### Backend Anti-Patterns
- **Entities niemals direkt im Controller-Response zurückgeben** – immer DTOs verwenden
- **Keine Business-Logik in Controller** – gehört in Service-Layer
- **Keine Business-Logik in Entities** – Entities sind nur Datenstrukturen
- **Keine direkten SQL-Queries in Services** – nutze Repository-Methoden
- **Kein `System.out.println()` für Logging** – verwende SLF4J Logger
- **Keine hartcodierten Strings für Fehlermeldungen** – nutze Constants oder i18n
- **Transaktionen niemals vergessen** bei schreibenden Operationen
- **Nie `Double` oder `BigDecimal` für Preise verwenden** – nur `Float` wie spezifiziert

#### Frontend Anti-Patterns
- **Keine Class-Komponenten** – nur funktionale Komponenten mit Hooks
- **Kein direktes DOM-Manipulation** – nutze React State
- **Kein `var` für Variablen** – immer `const` oder `let`
- **Keine Inline-Styles** ohne guten Grund – nutze CSS-Module oder Styled-Components
- **Fetch ohne Error-Handling** ist verboten
- **Props-Drilling vermeiden** – nutze Context API oder State Management für tief verschachtelte Daten
- **Kein unnötiges Re-Rendering** – nutze `useMemo`, `useCallback`, `React.memo`

### Performance-Optimierungen

#### Backend Performance
```java
// Lazy Loading für große Collections
@Entity
public class Meal {
    @OneToMany(mappedBy = "meal", fetch = FetchType.LAZY)
    private List<Order> orders;
}

// Pagination für große Datensätze
public interface MealRepository extends JpaRepository<Meal, Integer> {
    Page<Meal> findByDeletedFalse(Pageable pageable);
}

// ReadOnly-Transaktionen für Lesezugriffe
@Transactional(readOnly = true)
public List<MealDTO> getAllMeals() {
    return mealRepository.findAll().stream()
        .map(mealMapper::toDTO)
        .toList();
}
```

#### Frontend Performance
```javascript
// Lazy Loading für Routen
import { lazy, Suspense } from 'react';

const AdminPanel = lazy(() => import('./AdminPanel'));

function App() {
    return (
        <Suspense fallback={<div>Lädt...</div>}>
            <AdminPanel />
        </Suspense>
    );
}

// Memoization für teure Berechnungen
import { useMemo } from 'react';

const Dashboard = ({ orders }) => {
    const totalRevenue = useMemo(() => {
        return orders.reduce((sum, order) => sum + order.price, 0);
    }, [orders]);
    
    return <div>Gesamtumsatz: {totalRevenue}€</div>;
};
```

### Commit-Message-Standards

Nutze konventionelle Commit-Messages auf Deutsch:

```
[Feature] Soft Delete für Gerichte implementiert
[Bugfix] Fehler bei MealPlan Composite Key behoben
[Refactor] MealService in kleinere Methoden aufgeteilt
[Docs] API-Dokumentation für Order-Endpoints aktualisiert
[Test] Unit-Tests für MealService hinzugefügt
[Perf] Lazy Loading für Meal-Orders implementiert
[Style] Code-Formatierung nach Checkstyle-Regeln angepasst
```

### Build & Deployment Checkliste

#### Backend Build
```bash
# Dependencies installieren/aktualisieren
./mvnw clean install

# Tests ausführen
./mvnw test

# Test-Coverage prüfen
./mvnw jacoco:report

# App starten
./mvnw spring-boot:run

# Production Build
./mvnw clean package -DskipTests
```

#### Frontend Build
```bash
# Dependencies installieren
npm install

# Development Server
npm start

# Production Build
npm run build

# Tests ausführen
npm test

# Test Coverage
npm test -- --coverage

# Linting
npm run lint
```

### Dokumentation aktualisieren

Bei neuen Features IMMER aktualisieren:
- `README.md` im Projekt-Root
- `API-Endpoints.md` für neue REST-Endpoints
- JavaDoc für alle öffentlichen Service-Methoden
- JSDoc für komplexe Frontend-Funktionen
- Diese `copilot-instructions.md` bei neuen Patterns

### Zusammenfassung – Die 10 wichtigsten Regeln

1. **3-Schichten-Architektur STRIKT einhalten** – keine Vermischung!
2. **Code Englisch, Kommentare/Fehler Deutsch** – keine Ausnahmen
3. **Typen wie spezifiziert**: `Integer` für IDs, `Float` für Preise
4. **DTOs IMMER verwenden** – niemals Entities im HTTP-Response
5. **Custom Exceptions mit GlobalExceptionHandler** – keine direkten Error-Responses
6. **Soft Delete für Meals** – für historische Datenintegrität
7. **`@Transactional` für schreibende Ops** – `readOnly=true` für Lesezugriffe
8. **Funktionale React-Komponenten** mit Hooks – keine Class-Komponenten
9. **Tests für alle neuen Features** – Backend & Frontend
10. **Dokumentation aktuell halten** – Code ist nicht selbsterklärend genug

### Zusätzliche Ressourcen

- Spring Boot Docs: https://docs.spring.io/spring-boot/docs/3.5.6/reference/html/
- React Docs: https://react.dev/
- H2 Database: https://www.h2database.com/html/main.html
- JPA Best Practices: https://thorben-janssen.com/tips-to-boost-your-hibernate-performance/
- React Testing Library: https://testing-library.com/docs/react-testing-library/intro/