# RENTACAR – KI-Coding Assistant Instructions

## 🎯 Project Overview

**RENTACAR** is a backend-only information system for a mid-sized car rental company with multiple branches. The system manages vehicles, customers, bookings, and rental processes (check-out / check-in, damage reports, pricing).

> Important: **Backend-only project!** No web frontend; focus on clean DDD, REST API and high test coverage.

### Core Functional Requirements (Short Overview)

- **Vehicle Management**: Vehicle types, attributes, status (available, rented, maintenance, out of service).
- **Customer Management**: Registration, profile data, booking history.
- **Booking Management**: Search for available vehicles, create/modify/cancel bookings, price calculation.
- **Rental Process**: Check-out, check-in, mileage tracking, damage reports, extra fees.

### Non-Functional Requirements (Extract)

- **Performance**: Vehicle availability search ≤ 2 seconds; support ≥ 100 concurrent users.
- **Security**: GDPR-compliant storage, RBAC (customer, employee, admin), audit logging.
- **Maintainability**: Strict DDD, modular architecture, ≥ 80% unit test coverage.

---

## 🧠 MCP Servers: Context7 & Sequential Thinking

We use **MCP servers** consistently during development:

### 1. Context7 – Documentation & Examples

**Purpose**: Pull up-to-date, version-specific framework/library docs and code examples directly into the prompt.

**Use Context7 when:**

- You need exact usage of Spring Boot / JPA / Validation / Security APIs.
- You are unsure about annotations, config options or best practices.
- You design persistence mappings (e.g. `@ManyToOne`, `@Embeddable`) or transactions.

**Instruction to Copilot/ChatGPT (example):**

> “Use the MCP server **Context7** to look up the current Spring Boot 3 / Spring Data JPA documentation for X and integrate the recommended pattern into the generated code.”

### 2. Sequential Thinking – Stepwise Implementation Planning

**Purpose**: Create and follow a step-by-step plan for complex tasks and refactorings.

**Use Sequential Thinking when:**

- Designing a new aggregate (e.g. `Booking` or `RentalAgreement`).
- Implementing a complete feature end-to-end (Domain → Application → REST).
- Refactoring an existing flow based on quality-assurance feedback.

**Instruction to Copilot/ChatGPT (example):**

> “Use the MCP server **Sequential Thinking** to first generate a detailed step-by-step implementation plan for this feature. Then implement the steps one by one, updating the plan as you go.”

### 3. Mandatory MCP Usage Rules

- For **every non-trivial feature**, start with a **Sequential Thinking** plan.
- For **framework-specific questions**, always consult **Context7** before “guessing” APIs.
- When you ask the AI for code, explicitly remind it to:
  - Respect our DDD rules (see below).
  - Use MCP (Context7 / Sequential Thinking) where helpful.

---

## 🏗️ Architecture: DDD Layered Architecture

We follow a DDD-style layered architecture for the RENTACAR backend.

```text
src/main/java/com/rentacar/
├── domain/                # Domain Layer (core business logic)
│   ├── model/             # Entities, Value Objects, Enums
│   ├── repository/        # Repository interfaces (Ports)
│   └── service/           # Domain Services
├── application/           # Application Layer (Use Cases)
│   ├── service/           # Application Services
│   └── dto/               # Application DTOs (if needed)
├── infrastructure/        # Infrastructure Layer (Adapters)
│   ├── persistence/       # JPA repositories, DB mappings
│   ├── external/          # External systems (e.g. payments, identity)
│   └── messaging/         # Async messaging if needed
└── presentation/          # Presentation Layer (REST API)
    ├── controller/        # REST Controllers
    └── dto/               # Request/Response DTOs
```

**Golden Rules:**

- Domain entities **never** exposed directly via REST – always map to DTOs.
- Dependencies always **inward** toward domain:
  - `presentation → application → domain`
  - `infrastructure` implements ports for domain (repositories, external services).
- No Spring annotations in `domain` package (pure Java).

---

## 🚗 RENTACAR Domain Model

### Aggregates & Entities (initial suggestion)

- **Vehicle** (Aggregate Root)  
  Fields:  
  `id`, `licensePlate`, `brand`, `model`, `year`, `mileage`, `vehicleType`, `status`, `branch`.

  Status: `AVAILABLE`, `RENTED`, `IN_MAINTENANCE`, `OUT_OF_SERVICE`.

  Business methods:

  - `markAsRented()`
  - `markAsAvailable()`
  - `markAsInMaintenance()`
  - `retire()`

- **Customer** (Aggregate Root)  
  Fields:  
  `id`, `name`, `address`, `driverLicenseNumber`, `contactDetails`.

  Methods:

  - `updateContactDetails(...)`
  - `verifyDriverLicense(...)`

- **Branch** (Aggregate Root or Value Object reference)  
  Fields:  
  `id`, `name`, `address`, `openingHours`.

- **Booking** (Aggregate Root)  
  Fields:  
  `id`, `customer`, `vehicle`, `pickupBranch`, `returnBranch`,  
  `pickupDateTime`, `returnDateTime`, `status`, `totalPrice`, `options` (extras).

  Status: `REQUESTED`, `CONFIRMED`, `CANCELLED`, `EXPIRED`.

  Business rules: availability checks, cancellation up to 24h before pickup.

  Methods:

  - `confirm()`
  - `cancel()`
  - `expire()`
  - `calculatePrice(PricingPolicy)`

- **RentalAgreement** (Aggregate Root for actual rental)  
  Fields:  
  `id`, `booking`, `checkoutOdometer`, `checkinOdometer`,  
  `checkoutTime`, `checkinTime`, `status`, `finalPrice`, `damageReports`.

  Status: `OPEN`, `CLOSED`.

  Methods:

  - `checkOut(...)`
  - `checkIn(...)`
  - `registerDamage(...)`
  - `calculateFinalPrice(...)`

- **DamageReport**  
  Fields:  
  `id`, `rentalAgreement`, `description`, `severity`, `estimatedCost`, `photos`.

  Methods:

  - `estimateCost(...)`.

### Value Objects

- `Money` (amount + currency).
- `DateRange` for booking periods.
- `LicensePlate`, `Address`, `ContactDetails`.
- Optional: `Mileage` / `Kilometers` VO to validate non-negative values.

---

## 🔧 Tech Stack

- **Framework**: Spring Boot 3.x
- **Language**: Java 21+ (or course-specific version)
- **Persistence**: Spring Data JPA (H2 dev, PostgreSQL or similar for prod)
- **Security**: Spring Security with JWT and roles (`CUSTOMER`, `EMPLOYEE`, `ADMIN`)
- **Build**: Maven or Gradle
- **Testing**: JUnit 5, Mockito, MockMvc
- **Documentation**: OpenAPI / Swagger

---

## 📦 DDD Patterns (Adapted to RENTACAR)

### 1. Aggregates

Example (simplified) `Booking`:

```java
@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Customer customer;

    @ManyToOne(optional = false)
    private Vehicle vehicle;

    @Embedded
    private DateRange rentalPeriod;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Embedded
    private Money totalPrice;

    protected Booking() {
        // for JPA
    }

    public Booking(Customer customer,
                   Vehicle vehicle,
                   DateRange rentalPeriod,
                   Money totalPrice) {
        // validate invariants here
        this.customer = Objects.requireNonNull(customer);
        this.vehicle = Objects.requireNonNull(vehicle);
        this.rentalPeriod = Objects.requireNonNull(rentalPeriod);
        this.totalPrice = Objects.requireNonNull(totalPrice);
        this.status = BookingStatus.REQUESTED;
    }

    public void confirm() {
        if (status != BookingStatus.REQUESTED) {
            throw new BookingStatusTransitionException(id, status, BookingStatus.CONFIRMED);
        }
        this.status = BookingStatus.CONFIRMED;
    }

    public void cancel(LocalDateTime now) {
        validateCancellationWindow(now);
        if (!status.isCancellable()) {
            throw new BookingStatusTransitionException(id, status, BookingStatus.CANCELLED);
        }
        this.status = BookingStatus.CANCELLED;
    }

    // Private invariants & validation methods...
}
```

> Rule: **No public setters** for critical state; use expressive business methods instead.

### 2. Repository Pattern

Domain layer:

```java
public interface BookingRepository {
    Booking save(Booking booking);
    Optional<Booking> findById(Long id);
    List<Booking> findByCustomerId(Long customerId);
    List<Booking> findActiveBookingsForVehicle(Long vehicleId, DateRange period);
}
```

Infrastructure layer:

```java
@Repository
public interface JpaBookingRepository
        extends BookingRepository, JpaRepository<Booking, Long> {

    // Spring Data JPA derives the implementation
}
```

Same idea for `VehicleRepository`, `CustomerRepository`, `RentalAgreementRepository`, `DamageReportRepository`.

### 3. Domain Services

For logic that spans multiple aggregates (e.g., checking availability + pricing):

```java
@Service
public class BookingDomainService {

    public Booking createBooking(
        Customer customer,
        Vehicle vehicle,
        DateRange rentalPeriod,
        PricingPolicy pricingPolicy
    ) {
        validateVehicleAvailability(vehicle, rentalPeriod);
        Money price = pricingPolicy.calculatePrice(vehicle, rentalPeriod);
        return new Booking(customer, vehicle, rentalPeriod, price);
    }

    private void validateVehicleAvailability(Vehicle vehicle, DateRange rentalPeriod) {
        // domain rule checks...
    }
}
```

Application layer orchestrates calls to domain services + repositories.

---

## 🌐 REST API Conventions (Example)

- `GET /api/vehicles`
- `GET /api/vehicles/{id}`
- `POST /api/vehicles`
- `PUT /api/vehicles/{id}`
- `PATCH /api/vehicles/{id}/status`

- `GET /api/customers/{id}/bookings`
- `POST /api/bookings/search-availability`
- `POST /api/bookings`
- `POST /api/bookings/{id}/confirm`
- `POST /api/bookings/{id}/cancel`

- `POST /api/rentals/{bookingId}/check-out`
- `POST /api/rentals/{id}/check-in`
- `POST /api/rentals/{id}/damage-reports`

Conventions:

- Use DTOs for requests/responses.
- Validate DTOs with Bean Validation (`@Valid`, `@NotNull`, `@Future`, etc.).
- Map DTO ↔ Entity using dedicated mapper classes.

---

## 🔒 Security & Roles

Roles (minimum):

- `ROLE_CUSTOMER`
- `ROLE_EMPLOYEE`
- `ROLE_ADMIN`

Examples:

- `CUSTOMER` can:
  - register, manage own profile.
  - search vehicles, create/cancel own bookings.
- `EMPLOYEE` can:
  - manage vehicles, perform check-in/check-out, create damage reports.
- `ADMIN` can:
  - manage branches, adjust pricing policies, view audit logs.

Use method-level security where appropriate, e.g.:

```java
@PreAuthorize("hasRole('EMPLOYEE')")
public RentalAgreement performCheckOut(Long bookingId, CheckOutCommand command) {
    // ...
}
```

---

## 🧪 Testing Strategy

- **Unit tests** for:
  - Domain entities (state transitions, invariants).
  - Domain services (pricing, availability logic).
- **Integration tests** for:
  - REST controllers (MockMvc or WebTestClient).
  - Persistence mappings (H2 in-memory DB).

Target: **≥ 80%** unit test coverage, focusing on domain logic.

---

## 🌍 Language & Conventions

- **Code**: English (classes, methods, variables)
- **Comments**: German
- **Error messages**: German
- **Commit messages**: German
- **Documentation**: German (unless explicitly required otherwise)

---

## ✅ DDD Validation & Quality Checklist (Must Do!)

Before committing, check:

### Entities

- [ ] Entity has **business methods** (not just getters/setters).
- [ ] No public setters for critical fields.
- [ ] Invariants are validated **inside** the entity (constructor or methods).
- [ ] Domain-specific exceptions (e.g. `BookingStatusTransitionException`), not generic ones.
- [ ] Aggregate root protects its children (no external mutation of collections).

### Value Objects

- [ ] All fields `final`, no public setters.
- [ ] Validation happens in constructor/factory.
- [ ] No identity/ID fields.
- [ ] `equals()` / `hashCode()` correctly implemented (or Lombok `@Value`).

### Services

- [ ] Application services orchestrate use cases, do not contain core business rules.
- [ ] Core business logic lives in entities or domain services.
- [ ] Domain services are stateless and focused.
- [ ] `@Transactional` on application/domain services where needed.

### Repositories

- [ ] Repository interfaces in `domain.repository`.
- [ ] Spring Data / JPA implementations in `infrastructure.persistence`.
- [ ] Repositories return entities, not DTOs.
- [ ] No business logic in queries.

---

## 🤖 Copilot Prompt Template (with MCP)

Use this template whenever you ask the AI to generate code for RENTACAR:

> I am working on a **DDD (Domain-Driven Design)** backend project called **RENTACAR**  
> (car rental system with vehicles, customers, bookings, rentals and damage reports).
>
> Please:
>
> 1. Use the **Context7 MCP server** to look up up-to-date Spring Boot 3 / JPA / Validation / Security documentation as needed.
> 2. Use the **Sequential Thinking MCP server** to first create a step-by-step plan for the implementation, then implement it step by step.
>
> Generate the following code:
> [YOUR REQUEST, e.g. “Create the Booking aggregate with methods confirm() and cancel() and a BookingRepository interface.”]
>
> **CRITICAL DDD RULES (do NOT violate):**
>
> - **ENTITIES** have business methods, no public setters for critical fields.  
>   Example: `booking.confirm()` instead of `booking.setStatus(CONFIRMED)`.
> - **INVARIANTS** are validated inside the entity (constructor or methods),  
>   not in services or controllers.
> - **VALUE OBJECTS** are immutable (final fields, no setters).  
>   Validation happens in constructor or factory.
> - **SERVICES** orchestrate and call entity/domain-service methods,  
>   they should not implement complex business rules themselves.
> - **EXCEPTIONS** are domain-specific  
>   (e.g. `InvalidRentalPeriodException`, `BookingStatusTransitionException`),  
>   not generic (`IllegalArgumentException`).
> - **REPOSITORIES** are abstract interfaces in the domain layer and implemented  
>   in the infrastructure layer. They return **entities**, not DTOs.
> - **AGGREGATE ROOTS** protect their children. Child entities are only modified via the root.
>
> After generating the code, please:
>
> - Briefly validate against DDD best practices and the checklist above.
> - Suggest small improvements if you see any anemic domain model tendencies.

---

## 🔄 Recommended Workflow per Feature

1. **Clarify the use case** (e.g. “Customer creates a booking”).
2. **Ask Sequential Thinking** (via MCP) to generate a step-by-step plan.
3. **Design/adjust the domain model** (entities, VOs, repositories).
4. **Generate code** layer by layer:
   - Domain model
   - Domain services
   - Application services
   - REST controllers + DTOs
5. **Use Context7** (via MCP) to verify framework usage, libraries and edge cases.
6. **Write tests** (domain first, then REST).
7. **Run tests, refactor, re-run**.
8. **Commit only when the DDD checklist is fulfilled.**
