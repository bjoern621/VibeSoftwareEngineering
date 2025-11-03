# TRAVELREIMBURSE – KI-Coding-Assistent Anweisungen

## 🎯 Projektübersicht

**TRAVELREIMBURSE** ist ein Reisekostenabrechnungssystem zur Verwaltung von Dienstreisen. Das System unterstützt Beantragung, Genehmigung, Belegverwaltung und automatische Abrechnung nach Reiserichtlinien.

**Wichtig**: Backend-only Projekt! Kein Frontend erforderlich.

### Externe Systeme

- **HRIS**: HR-System für Abwesenheiten/Urlaubszeiten
- **EasyPay**: Finanzsystem für Auszahlung
- **ExRat**: Währungskurse-Service

### Nutzerrollen

- **EMPLOYEE**: Reisen beantragen, Belege hochladen
- **MANAGER**: Anträge genehmigen/ablehnen
- **HR**: Auswertungen, Reiserichtlinien verwalten
- **ASSISTANT**: Im Namen anderer handeln (Delegation)
- **FINANCE**: Finale Freigabe zur Auszahlung

---

## 🏗️ Architektur: Domain-Driven Design (Layered Architecture)

Das Projekt folgt **striktem DDD** mit Layered Architecture:

```
src/main/java/com/travelreimburse/
├── domain/                # Domain Layer (Kern-Logik)
│   ├── model/            # Entities, Value Objects, Enums
│   ├── repository/       # Repository-Interfaces
│   └── service/          # Domain Services
├── application/          # Application Layer (Use Cases)
│   ├── service/          # Application Services
│   └── dto/              # DTOs für Datenübertragung
├── infrastructure/       # Infrastructure Layer (Technik)
│   ├── persistence/      # JPA-Repository-Implementierungen
│   ├── external/         # Externe APIs (HRIS, EasyPay, ExRat)
│   └── email/            # E-Mail-Service
└── presentation/         # Presentation Layer (REST API)
    ├── controller/       # REST-Controller
    └── dto/              # Request/Response DTOs
```

### Schichten-Verantwortlichkeiten

**Domain Layer** (Kern):
- Entities: `TravelRequest`, `Expense`, `Receipt`, `Employee`, `TravelPolicy`
- Value Objects: `Money`, `DateRange`, `Address`
- Repository-Interfaces (abstrakt)
- Domain Services für komplexe Geschäftslogik

**Application Layer**:
- Orchestrierung von Use Cases
- Services: `TravelRequestService`, `ExpenseService`, `ApprovalService`

**Infrastructure Layer**:
- JPA-Repository-Implementierungen
- REST-Clients für externe Systeme
- Technische Services (E-Mail, File Storage)

**Presentation Layer**:
- REST-Controller mit DTOs
- Exception Handling (`@ControllerAdvice`)

**Goldene Regel**: Domain-Entities **niemals** direkt in der Presentation Layer verwenden! Immer DTOs.

---

## 📦 Wichtige Domain-Patterns

### 1. Aggregate Pattern

**TravelRequest** ist das Hauptaggregat (Aggregate Root):

```java
@Entity
public class TravelRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private DateRange travelPeriod;

    @Embedded
    private Money estimatedCost;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Expense> expenses = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Receipt> receipts = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private TravelRequestStatus status;

    // Business-Methoden für Zustandsänderungen
    public void submit() {
        if (status != TravelRequestStatus.DRAFT) {
            throw new IllegalStateException("Nur Entwürfe können eingereicht werden");
        }
        this.status = TravelRequestStatus.SUBMITTED;
    }

    public void approve(Employee approver) {
        validateCanBeApproved();
        this.status = TravelRequestStatus.APPROVED;
        this.approver = approver;
        this.approvedAt = LocalDateTime.now();
    }
}
```

### 2. Value Objects Pattern

Unveränderbare Werteobjekte ohne Identität:

```java
@Embeddable
public class Money {
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    public Money(BigDecimal amount, Currency currency) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Betrag darf nicht negativ sein");
        }
        this.amount = amount;
        this.currency = currency;
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Währungen müssen übereinstimmen");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    // Immutable: keine Setter!
}
```

```java
@Embeddable
public class DateRange {
    private LocalDate startDate;
    private LocalDate endDate;

    public DateRange(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Enddatum muss nach Startdatum liegen");
        }
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public long getDays() {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
}
```

### 3. Repository Pattern

**Domain Layer** (Interface):
```java
public interface TravelRequestRepository {
    TravelRequest save(TravelRequest request);
    Optional<TravelRequest> findById(Long id);
    List<TravelRequest> findByEmployeeId(Long employeeId);
    List<TravelRequest> findPendingApprovalsByManagerId(Long managerId);
}
```

**Infrastructure Layer** (Implementierung):
```java
@Repository
public interface JpaTravelRequestRepository 
    extends TravelRequestRepository, JpaRepository<TravelRequest, Long> {
    // Spring Data JPA generiert Implementierung
}
```

### 4. Domain Service Pattern

Für Logik, die nicht zu einer Entity gehört:

```java
@Service
public class TravelPolicyValidator {

    public ValidationResult validate(TravelRequest request, TravelPolicy policy) {
        List<String> violations = new ArrayList<>();

        // Prüfe Höchstbeträge pro Kategorie
        for (Expense expense : request.getExpenses()) {
            Money maxAmount = policy.getMaxAmountForCategory(expense.getCategory());
            if (expense.getAmount().isGreaterThan(maxAmount)) {
                violations.add("Ausgabe überschreitet Höchstbetrag");
            }
        }

        return new ValidationResult(violations.isEmpty(), violations);
    }
}
```

### 5. Status-Maschine

```java
public enum TravelRequestStatus {
    DRAFT,                      // Entwurf
    SUBMITTED,                  // Eingereicht
    APPROVED,                   // Genehmigt
    REJECTED,                   // Abgelehnt
    IN_PROGRESS,                // Läuft aktuell
    COMPLETED,                  // Abgeschlossen
    REIMBURSEMENT_PENDING,      // Abrechnung wartet
    REIMBURSEMENT_APPROVED,     // Abrechnung genehmigt
    PAID,                       // Ausgezahlt
    ARCHIVED                    // Archiviert
}
```

Validierung der Zustandsübergänge in Entity-Methoden!

---

## 🔧 Tech Stack

- **Framework**: Spring Boot 3.x
- **Java**: 17+
- **Datenbank**: H2 (Dev), PostgreSQL (Prod)
- **Persistierung**: Spring Data JPA
- **Security**: Spring Security mit JWT
- **Build-Tool**: Maven
- **Testing**: JUnit 5, Mockito, MockMvc

---

## 📋 Wichtige Typ-Konventionen

- **IDs**: `Long` (nicht Integer)
- **Geldbeträge**: `BigDecimal` (niemals Float/Double!)
- **Währungen**: `Currency` Enum
- **Datum**: `LocalDate`
- **Zeitstempel**: `LocalDateTime`

**Kritisch**: Bei Geldbeträgen **immer** `BigDecimal` verwenden wegen Rundungsfehlern!

---

## 🌐 REST API Conventions

```
GET    /api/travel-requests              # Liste aller Anträge
GET    /api/travel-requests/{id}         # Einzelner Antrag
POST   /api/travel-requests              # Neuer Antrag
PUT    /api/travel-requests/{id}         # Aktualisieren
DELETE /api/travel-requests/{id}         # Löschen

POST   /api/travel-requests/{id}/submit  # Einreichen
POST   /api/travel-requests/{id}/approve # Genehmigen
POST   /api/travel-requests/{id}/reject  # Ablehnen

POST   /api/travel-requests/{id}/expenses    # Ausgabe hinzufügen
POST   /api/travel-requests/{id}/receipts    # Beleg hochladen (Multipart)
```

---

## 🔒 Security & Rollen

JWT-basierte Authentifizierung mit Method-Level Security:

```java
@PreAuthorize("hasRole('EMPLOYEE')")
public TravelRequest createTravelRequest(CreateTravelRequestDTO dto, Long employeeId) {
    // Nur eigener Employee
    validateEmployeeMatchesCurrentUser(employeeId);
    // ...
}

@PreAuthorize("hasRole('MANAGER')")
public TravelRequest approveTravelRequest(Long requestId, Long managerId) {
    // Nur zuständiger Manager
    // ...
}

@PreAuthorize("hasRole('HR')")
public List<TravelRequest> getAllForReporting() {
    // Nur HR
    // ...
}
```

**Delegation**: Assistenten können im Namen anderer handeln (eigene Entity `Delegation`).

---

## 🔌 Externe Integrationen

### HRIS-Client (HR-System)
```java
@Service
public class HrisClient {
    public Optional<AbsenceInfo> checkAbsence(Long employeeId, DateRange period) {
        // REST-Call zu HRIS-API
        // Prüft ob Mitarbeiter im Urlaub ist
    }
}
```

### EasyPay-Client (Auszahlung)
```java
@Service
public class EasyPayClient {
    public PaymentResult submitPayment(TravelRequest request, Money totalAmount) {
        // REST-Call zu EasyPay-API
        // Veranlasst Auszahlung
    }
}
```

### ExRat-Client (Währungskurse)
```java
@Service
public class ExRatClient {
    @Cacheable(value = "exchangeRates")
    public ExchangeRate getExchangeRate(Currency from, Currency to, LocalDate date) {
        // REST-Call zu ExRat-API
        // Holt aktuellen Wechselkurs
    }
}
```

**Wichtig**: Caching für Währungskurse nutzen!

---

## 📝 Exception Handling

Custom Exceptions im Domain Layer:

```java
public class TravelRequestNotFoundException extends RuntimeException {
    public TravelRequestNotFoundException(Long id) {
        super("Reiseantrag mit ID " + id + " nicht gefunden");
    }
}

public class TravelPolicyViolationException extends RuntimeException {
    private final List<String> violations;
    // ...
}

public class InsufficientPermissionException extends RuntimeException {
    // ...
}
```

Global Exception Handler:

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TravelRequestNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(TravelRequestNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(TravelPolicyViolationException.class)
    public ResponseEntity<ErrorResponse> handlePolicyViolation(TravelPolicyViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse("POLICY_VIOLATION", ex.getMessage(), ex.getViolations()));
    }
}
```

---

## 📁 Datei-Upload (Belege)

Multipart-Upload für PDF/Bilder:

```java
@PostMapping(value = "/api/receipts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<ReceiptDTO> uploadReceipt(
    @RequestParam("file") MultipartFile file,
    @RequestParam("travelRequestId") Long travelRequestId,
    @RequestParam("description") String description
) {
    // Validierung: Max 10MB, nur PDF/JPG/PNG
    validateFile(file);

    Receipt receipt = receiptService.uploadReceipt(file, travelRequestId, description);
    return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(receipt));
}
```

---

## ✅ Validation

Bean Validation in DTOs:

```java
public record CreateTravelRequestDTO(
    @NotBlank(message = "Ziel darf nicht leer sein")
    String destination,

    @NotBlank(message = "Zweck erforderlich")
    String purpose,

    @NotNull @FutureOrPresent
    LocalDate startDate,

    @NotNull
    LocalDate endDate,

    @NotNull @DecimalMin("0.01")
    BigDecimal estimatedAmount,

    @NotNull
    Currency currency
) {
    // Custom Validation im Constructor
    public CreateTravelRequestDTO {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Enddatum muss nach Startdatum liegen");
        }
    }
}
```

Controller:
```java
@PostMapping
public ResponseEntity<TravelRequestDTO> create(
    @Valid @RequestBody CreateTravelRequestDTO dto  // @Valid triggert Validation
) {
    // ...
}
```

---

## 🧪 Testing

### Unit Tests (Domain Services)
```java
@Test
void shouldValidateTravelPolicyViolation() {
    // Given
    TravelRequest request = createRequestWithHighExpenses();
    TravelPolicy policy = createStrictPolicy();

    // When
    ValidationResult result = validator.validate(request, policy);

    // Then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getViolations()).hasSize(2);
}
```

### Integration Tests (Controller)
```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TravelRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void shouldCreateTravelRequest() throws Exception {
        mockMvc.perform(post("/api/travel-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }
}
```

---

## 🗂️ DTO-Mapping

**Mapper-Pattern** für Entity ↔ DTO:

```java
@Component
public class TravelRequestMapper {

    public TravelRequestDTO toDTO(TravelRequest entity) {
        return new TravelRequestDTO(
            entity.getId(),
            entity.getEmployee().getId(),
            entity.getDestination(),
            entity.getPurpose(),
            entity.getTravelPeriod().getStartDate(),
            entity.getTravelPeriod().getEndDate(),
            entity.getStatus().name()
        );
    }

    public TravelRequest toEntity(CreateTravelRequestDTO dto, Employee employee) {
        return TravelRequest.builder()
            .employee(employee)
            .destination(dto.destination())
            .purpose(dto.purpose())
            .travelPeriod(new DateRange(dto.startDate(), dto.endDate()))
            .estimatedCost(new Money(dto.estimatedAmount(), dto.currency()))
            .status(TravelRequestStatus.DRAFT)
            .build();
    }
}
```

---

## 💾 Transaktions-Management

```java
@Service
@Transactional(readOnly = true)  // Default für Lese-Operationen
public class TravelRequestService {

    @Transactional  // Schreibende Operation
    public TravelRequest submitTravelRequest(Long requestId) {
        TravelRequest request = findById(requestId);
        request.submit();

        TravelRequest saved = repository.save(request);
        emailService.sendSubmittedNotification(saved);

        return saved;
    }

    // readOnly = true für Performance
    public List<TravelRequest> findAllByEmployee(Long employeeId) {
        return repository.findByEmployeeId(employeeId);
    }
}
```

---

## 🎨 Clean Code Principles

1. **Kleine Methoden**: Max 20 Zeilen
2. **Single Responsibility**: Eine Methode = eine Aufgabe
3. **Sprechende Namen**: `validateTravelPolicy()` statt `check()`
4. **Keine Magic Numbers**: Konstanten verwenden
5. **Early Return**: Guard Clauses am Anfang
6. **Immutability**: Value Objects unveränderlich

**Beispiel - Gut**:
```java
public void approveTravelRequest(TravelRequest request) {
    validateRequestNotNull(request);
    validateStatusIsSubmitted(request);
    validateManagerIsResponsible(request);

    performApproval(request);
    sendApprovalNotification(request);
}
```

**Beispiel - Schlecht**:
```java
public void approve(TravelRequest r) {
    if (r != null) {
        if (r.getStatus() == TravelRequestStatus.SUBMITTED) {
            // 50 Zeilen verschachtelter Code...
        }
    }
}
```

---

## 📚 Dokumentation

### OpenAPI/Swagger

Swagger UI verfügbar unter: `http://localhost:8080/swagger-ui.html`

```java
@RestController
@Tag(name = "Reiseanträge", description = "Verwaltung von Reiseanträgen")
public class TravelRequestController {

    @Operation(summary = "Reiseantrag erstellen")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Erfolgreich erstellt"),
        @ApiResponse(responseCode = "400", description = "Ungültige Daten")
    })
    @PostMapping
    public ResponseEntity<TravelRequestDTO> create(@Valid @RequestBody CreateTravelRequestDTO dto) {
        // ...
    }
}
```

---

## 🔄 Schrittweises Vorgehen

**Best Practice für neue Features**:

1. **Domain-Model** definieren (Entities, Value Objects)
2. **Repository-Interface** im Domain Layer erstellen
3. **Domain Service** für Business-Logik implementieren
4. **Application Service** als Use-Case implementieren
5. **REST-Controller** mit DTOs erstellen
6. **Tests** schreiben (Unit → Integration)
7. **Dokumentation** aktualisieren

---

## 🌍 Sprache & Konventionen

- **Code**: Englisch (Klassen, Methoden, Variablen)
- **Kommentare**: Deutsch
- **Fehlermeldungen**: Deutsch
- **Commit-Messages**: Deutsch
- **Dokumentation**: Deutsch

---

## ⚙️ Konfiguration

### application.properties (Development)
```properties
# H2 Database
spring.datasource.url=jdbc:h2:mem:travelreimburse
spring.jpa.hibernate.ddl-auto=create-drop
spring.h2.console.enabled=true

# External APIs
hris.api.url=http://localhost:9001/api
easypay.api.url=http://localhost:9002/api
exrat.api.url=http://localhost:9003/api

# File Upload
file.upload-dir=./uploads/receipts
spring.servlet.multipart.max-file-size=10MB

# JWT
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000
```

---

## 📦 Wichtige Dependencies

```xml
<dependencies>
    <!-- Spring Boot Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-mail</artifactId>
    </dependency>

    <!-- Database -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>

    <!-- OpenAPI/Swagger -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.3.0</version>
    </dependency>

    <!-- Lombok (Optional) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## 🚀 Quick Start

```bash
# Repository klonen
git clone <repository-url>
cd TravelReimburse

# Backend starten
cd backend
./mvnw clean install
./mvnw spring-boot:run

# API läuft auf http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
# H2 Console: http://localhost:8080/h2-console
```

---

## 🎓 Wichtige Konzepte für KI-Coding

### Kontext-Management
- **Teile große Aufgaben** in kleine Schritte auf
- **Ein Feature nach dem anderen** implementieren
- **Immer zuerst Domain-Model** dann Infrastructure

### Prompting-Strategie
1. "Erstelle Entity TravelRequest mit Feldern X, Y, Z"
2. "Erstelle Repository-Interface für TravelRequest"
3. "Implementiere Domain Service für Policy-Validierung"
4. "Erstelle Application Service für Use Case 'Reise beantragen'"
5. "Erstelle REST-Controller für TravelRequest"

### Qualitätssicherung
- Nach jedem Schritt: **Code Review** durch KI
- Frage explizit: "Entspricht das DDD Best Practices?"
- Teste Schritt für Schritt

---

## ✨ Zusammenfassung

**TRAVELREIMBURSE** folgt striktem **Domain-Driven Design**:

✅ Layered Architecture (Domain → Application → Infrastructure → Presentation)
✅ Tactical DDD (Aggregates, Value Objects, Repositories, Domain Services)
✅ Externe Integrationen (HRIS, EasyPay, ExRat)
✅ Security mit JWT und Rollen
✅ Clean Code & Best Practices
✅ Umfassende Validierung & Exception Handling

**Wichtigste Regel**: Domain-Logik gehört in den Domain Layer, nicht in Controller oder Services!

---

## 📖 Weiterführende Ressourcen

- **Buch**: "Domain-Driven Design Distilled" (Vaughn Vernon)
- **Spring Boot Docs**: https://spring.io/projects/spring-boot
- **DDD Reference**: https://domainlanguage.com/ddd/reference/

---

**Viel Erfolg beim Projekt! 🚀**
