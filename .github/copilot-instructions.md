# CONCERT COMPARISON – KI-Coding Assistant Instructions

## 🎯 Project Overview

**CONCERT COMPARISON** ist ein hochskalierbares Ticket-Verkaufssystem für große Konzerte und Events. Das System muss tausende gleichzeitige Nutzer handhaben und Spitzenlasten von bis zu 10.000 Requests/Sekunde beim Verkaufsstart bewältigen.

> Wichtig: **Full-Stack Projekt!** Backend (Spring Boot) + Frontend (React); Fokus auf Concurrency, Skalierbarkeit, DDD und hohe Testabdeckung.

### Core Functional Requirements

- **Seat Management**: Sitzplätze/Kategorien für Konzerte verwalten, Verfügbarkeit in Echtzeit anzeigen.
- **Seat Reservation (Hold)**: Zeitlich begrenzte Reservierung von Plätzen während des Checkouts (TTL).
- **Ticket Purchase**: Kaufabschluss nur für aktive Holds, kein doppelter Verkauf.
- **Concurrency Control**: Garantie, dass ein Platz maximal einmal verkauft wird (Race Condition Prevention).
- **Real-time Updates**: Live-Aktualisierung der Verfügbarkeit (Polling/SSE/WebSocket).
- **Event Comparison**: Konzerte vergleichen nach Datum, Ort, Preis, Verfügbarkeit.
- **Rate Limiting**: Schutz vor übermäßigen/automatisierten Zugriffen.
- **Admin Functions**: Konzerte und Seats anlegen.

### Non-Functional Requirements

- **Performance**: 
  - Verfügbarkeitsabfrage ≤ 1 Sekunde
  - Support für ≥ 1000 gleichzeitige Nutzer
  - Spitzenlasten bis 10.000 Requests/Sekunde
- **Reliability**: 
  - Kein Platz darf doppelt verkauft werden (atomare Transaktionen)
  - Automatische Hold-Freigabe nach TTL-Ablauf
- **Scalability**: 
  - Horizontal skalierbar (Stateless Backend)
  - Elastic Load Balancing
- **Quality**: 
  - Strict DDD, modular architecture
  - ≥ 80% unit test coverage
  - Automated concurrency tests
  - CI/CD Pipeline

---

## 🏗️ Architecture: DDD Layered Architecture

Wir folgen einer DDD-Style Layered Architecture für das Concert Comparison Backend.

```text
src/main/java/com/concertcomparison/
├── domain/                # Domain Layer (core business logic)
│   ├── model/             # Entities, Value Objects, Enums
│   ├── repository/        # Repository interfaces (Ports)
│   └── service/           # Domain Services
├── application/           # Application Layer (Use Cases)
│   ├── service/           # Application Services
│   └── dto/               # Application DTOs (if needed)
├── infrastructure/        # Infrastructure Layer (Adapters)
│   ├── persistence/       # JPA repositories, DB mappings
│   ├── messaging/         # SSE/WebSocket/Async messaging
│   ├── scheduler/         # Background jobs (Hold cleanup)
│   └── cache/             # Caching (Redis/Caffeine)
└── presentation/          # Presentation Layer (REST API, WebSocket)
    ├── controller/        # REST Controllers, WebSocket endpoints
    └── dto/               # Request/Response DTOs
```

**Goldene Regeln:**

- Domain Entities **niemals** direkt via REST exponieren – immer auf DTOs mappen.
- Dependencies immer **einwärts** zur Domain:
  - `presentation → application → domain`
  - `infrastructure` implementiert Ports für Domain (Repositories, External Services).
- Keine Spring Annotations im `domain` Package (pure Java).

---


## 🔧 Tech Stack

- **Framework**: Spring Boot 3.x
- **Language**: Java 21+
- **Persistence**: Spring Data JPA (H2 dev, PostgreSQL/MySQL prod)
- **Concurrency**: Optimistic Locking (`@Version`), Pessimistic Locking (wo nötig)
- **Caching**: Redis (prod) / Caffeine (dev) für Availability-Aggregation
- **Real-time**: Server-Sent Events (SSE) oder WebSocket für Live-Updates
- **Scheduling**: Spring `@Scheduled` für Hold-Cleanup-Jobs
- **Rate Limiting**: Bucket4j oder Spring Cloud Gateway
- **Security**: Spring Security mit JWT/Session
- **Build**: Maven
- **Testing**: JUnit 5, Mockito, Testcontainers (für Concurrency-Tests)
- **CI/CD**: GitHub Actions mit Tests, Code Coverage, Sonar
- **Documentation**: OpenAPI / Swagger

---

## 🔐 Security & Rate Limiting

### Authentication & Authorization

- **Methode**: JWT (stateless) oder Session-based
- **Rollen**: `USER`, `ADMIN`
- **Geschützte Endpoints**:
  - `POST /api/reservations` – USER
  - `POST /api/orders` – USER
  - `POST /api/events` – ADMIN
  - `POST /api/seats` – ADMIN

### Rate Limiting (US-08)

- **Implementierung**: Bucket4j (Token Bucket Algorithm)
- **Limits**: Konfigurierbar pro IP/User
  - z.B. 100 Requests/Minute für normale User
  - z.B. 10 Seat-Holds/Minute pro User
- **Response**: HTTP 429 Too Many Requests
- **Metriken**: Prometheus/Micrometer für Monitoring

---

## 🌐 REST API Conventions

### Events

- `GET /api/events` – Liste aller Events (mit Filtern)
- `GET /api/events/{id}` – Event-Details
- `POST /api/events` – Event erstellen (ADMIN)
- `PUT /api/events/{id}` – Event aktualisieren (ADMIN)
- `DELETE /api/events/{id}` – Event löschen (ADMIN)

### Seats

- `GET /api/events/{eventId}/seats` – Alle Seats für ein Event
- `GET /api/events/{eventId}/availability` – Verfügbarkeit aggregiert
- `POST /api/seats` – Seats bulk-erstellen (ADMIN)

### Reservations

- `POST /api/reservations` – Seat reservieren (Hold)
- `GET /api/reservations/{id}` – Reservation-Details
- `DELETE /api/reservations/{id}` – Reservation stornieren

### Orders

- `POST /api/orders` – Reservation kaufen (Checkout)
- `GET /api/orders/{id}` – Order-Details
- `GET /api/users/me/orders` – Meine Orders

### Conventions

- DTOs für Requests/Responses verwenden.
- Bean Validation (`@Valid`, `@NotNull`, `@Future`, etc.).
- DTO ↔ Entity Mapping via dedizierte Mapper-Klassen.
- Fehlerbehandlung via `@RestControllerAdvice`.

---

## 🧪 Testing Strategy

### Unit Tests

- **Domain Entities**: State Transitions, Invarianten
- **Domain Services**: Business-Logik (Hold, Sell, Expire)
- **Application Services**: Use-Case-Orchestrierung
- **Target**: ≥ 80% Coverage

### Integration Tests

- **REST Controllers**: MockMvc oder WebTestClient
- **Persistence**: H2 in-memory oder Testcontainers
- **Concurrency Tests**: ExecutorService, ParallelStreams
- **SSE/WebSocket**: WebTestClient mit Flux

### Concurrency Tests (MUST!)

```java
@SpringBootTest
class SeatConcurrencyTest {

    @Test
    void shouldHandleConcurrentReservations() {
        // Test mit 100+ concurrent requests
        // Assertion: Genau 1 erfolgreich, Rest Conflict/Error
    }
}
```

### CI/CD Pipeline

- **GitHub Actions**:
  - Build & Unit Tests
  - Integration Tests
  - Code Coverage Report (JaCoCo)
  - SonarQube Analysis
  - Docker Build (optional)

---

## 🌍 Language & Conventions

- **Code**: English (classes, methods, variables)
- **Comments**: German
- **Error messages**: German
- **Commit messages**: German
- **Documentation**: German

---

## ✅ DDD Validation & Quality Checklist

### Entities

- [ ] Entity hat **Business Methods** (nicht nur Getters/Setters).
- [ ] Keine public Setters für kritische Felder.
- [ ] Invarianten werden **innerhalb** der Entity validiert (Constructor oder Methods).
- [ ] Domain-spezifische Exceptions (z.B. `SeatNotAvailableException`), nicht generische.
- [ ] Aggregate Root schützt seine Children (keine externe Mutation von Collections).

### Value Objects

- [ ] Alle Felder `final`, keine public Setters.
- [ ] Validierung passiert im Constructor/Factory.
- [ ] Keine Identität/ID-Felder.
- [ ] `equals()` / `hashCode()` korrekt implementiert (oder Lombok `@Value`).

### Services

- [ ] Application Services orchestrieren Use Cases, enthalten keine Core Business Rules.
- [ ] Core Business Logik lebt in Entities oder Domain Services.
- [ ] Domain Services sind stateless und fokussiert.
- [ ] `@Transactional` auf Application/Domain Services wo nötig.

### Repositories

- [ ] Repository Interfaces in `domain.repository`.
- [ ] Spring Data / JPA Implementierungen in `infrastructure.persistence`.
- [ ] Repositories geben Entities zurück, nicht DTOs.
- [ ] Keine Business-Logik in Queries.

### Concurrency (CRITICAL!)

- [ ] Optimistic Locking (`@Version`) für alle Concurrency-kritischen Entities (Seat!).
- [ ] Concurrency-Tests implementiert (mind. 1 Test mit 50+ parallelen Threads).
- [ ] Race Conditions dokumentiert und getestet.
- [ ] TTL-basierte Hold-Freigabe implementiert und getestet.

---

## 🤖 Copilot Prompt Template (with MCP)

Nutze dieses Template, wann immer du die KI um Code für **CONCERT COMPARISON** bittest:

> Ich arbeite an einem **DDD (Domain-Driven Design)** Backend-Projekt namens **CONCERT COMPARISON**  
> (Ticket-Verkaufssystem für Konzerte mit hoher Concurrency und Skalierbarkeit).
>
> Bitte:
>
> 1. Nutze den **Context7 MCP Server** um aktuelle Spring Boot 3 / JPA / Validation / Security Dokumentation zu recherchieren.
> 2. Nutze den **Sequential Thinking MCP Server** um zuerst einen Schritt-für-Schritt-Plan zu erstellen, dann implementiere schrittweise.
>
> Generiere folgenden Code:
> [DEINE ANFRAGE, z.B. "Erstelle das Seat Aggregate mit hold(), release() und sell() Methoden sowie Optimistic Locking."]
>
> **KRITISCHE DDD & CONCURRENCY REGELN (NICHT VERLETZEN):**
>
> - **ENTITIES** haben Business Methods, keine public Setters für kritische Felder.  
>   Beispiel: `seat.hold(reservationId)` statt `seat.setStatus(HELD)`.
> - **INVARIANTS** werden innerhalb der Entity validiert (Constructor oder Methods),  
>   nicht in Services oder Controllers.
> - **VALUE OBJECTS** sind immutable (final fields, keine Setters).  
>   Validierung im Constructor oder Factory.
> - **SERVICES** orchestrieren und rufen Entity/Domain-Service Methoden auf,  
>   sie sollten keine komplexen Business Rules selbst implementieren.
> - **EXCEPTIONS** sind domain-spezifisch  
>   (z.B. `SeatNotAvailableException`, `ReservationExpiredException`),  
>   nicht generisch (`IllegalArgumentException`).
> - **REPOSITORIES** sind abstrakte Interfaces im Domain Layer, implementiert  
>   im Infrastructure Layer. Sie geben **Entities** zurück, nicht DTOs.
> - **AGGREGATE ROOTS** schützen ihre Children. Child-Entities werden nur via Root modifiziert.
> - **OPTIMISTIC LOCKING** (`@Version`) für alle Concurrency-kritischen Entities (z.B. Seat).
> - **TRANSAKTIONEN** (@Transactional) für alle State-Changes, insb. Hold → Sold Transitions.
> - **CONCURRENCY TESTS** für jeden kritischen Flow (Hold, Sell, Expire).
>
> Nach Code-Generierung bitte:
>
> - Kurz gegen DDD Best Practices und obige Checklist validieren.
> - Verbesserungsvorschläge machen, falls Anemic Domain Model Tendenzen erkennbar.

---

## 🔄 Recommended Workflow per Feature

1. **Clarify the Use Case** (z.B. "User reserviert einen Seat").
2. **Ask Sequential Thinking** (via MCP) um einen Schritt-für-Schritt-Plan zu generieren.
3. **Design/Adjust Domain Model** (Entities, VOs, Repositories).
4. **Generate Code** Layer für Layer:
   - Domain Model
   - Domain Services
   - Application Services
   - REST Controllers + DTOs
5. **Use Context7** (via MCP) um Framework-Nutzung, Libraries und Edge Cases zu verifizieren.
6. **Write Tests** (Domain zuerst, dann REST, dann Concurrency).
7. **Run Tests, Refactor, Re-Run**.
8. **Commit nur wenn DDD & Concurrency Checklist erfüllt.**

---

**Quality over Quantity!** Fokus auf saubere Analyse, Tests, Dokumentation und CI/CD. Starte mit einer simplen User Story und baue eine solide Basis!
