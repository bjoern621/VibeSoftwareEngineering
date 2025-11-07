# DDD Email Notification Refactoring

## Overview
This refactoring introduces Domain-Driven Design principles for email notifications:
- **Domain Events** for decoupling
- **Rich Domain Models** with business logic
- **Event Listeners** for side-effects
- **Async processing** for performance

## Changed Files

### Domain Layer
- `DomainEvent.java` - Base interface for all events
- `TravelRequestStatusChangedEvent.java` - Event for travel request status changes
- `ReceiptStatusChangedEvent.java` - Event for receipt status changes
- `InvalidStatusTransitionException.java` - Domain exception
- `TravelRequest.java` - Now extends AbstractAggregateRoot, adds updateStatus()
- `Receipt.java` - Now extends AbstractAggregateRoot, updates validate/reject/archive

### Application Layer
- `TravelRequestEventListener.java` - Async email notifications for travel requests
- `ReceiptEventListener.java` - Async email notifications for receipts
- `TravelRequestService.java` - Removed event publisher dependency, added userId parameters
- `ReceiptService.java` - Removed event publisher dependency, added userId parameters

### Presentation Layer
- `TravelRequestController.java` - Added user ID extraction (hardcoded for now)
- `ReceiptController.java` - Added user ID extraction (hardcoded for now)

### Infrastructure
- `AsyncConfig.java` - Enables async event processing

## Breaking Changes

### 1. TravelRequest Domain Model
```java
// OLD:
travelRequest.setStatus(TravelRequestStatus.APPROVED);

// NEW:
travelRequest.updateStatus(TravelRequestStatus.APPROVED, userId);
```

**Reason:** Status transitions now validate business rules and publish domain events.

### 2. Receipt Domain Model
```java
// OLD:
receipt.validate();
receipt.reject(reason);
receipt.archive();

// NEW:
receipt.validate(validatedByUserId);
receipt.reject(reason, rejectedByUserId);
receipt.archive(archivedByUserId);
```

**Reason:** Track who performed the action for audit trail and event publishing.

### 3. TravelRequestService
```java
// OLD:
travelRequestService.submitTravelRequest(id);

// NEW:
travelRequestService.submitTravelRequest(id, userId);
```

**Affected Methods:**
- `submitTravelRequest(id)` → `submitTravelRequest(id, userId)`

**Reason:** User ID required for domain events.

### 4. ReceiptService
```java
// OLD:
receiptService.validateReceipt(receiptId);
receiptService.rejectReceipt(receiptId, reason);

// NEW:
receiptService.validateReceipt(receiptId, validatedByUserId);
receiptService.rejectReceipt(receiptId, reason, rejectedByUserId);
```

**Affected Methods:**
- `validateReceipt(id)` → `validateReceipt(id, userId)`
- `rejectReceipt(id, reason)` → `rejectReceipt(id, reason, userId)`

**Reason:** User ID required for domain events and audit trail.

### 5. Controllers
All mutation endpoints now use hardcoded userId (temporary):
```java
@PostMapping("/{id}/submit")
public ResponseEntity<TravelRequestResponseDTO> submitTravelRequest(@PathVariable Long id) {
    Long userId = 1L; // TODO: Extract from Spring Security
    // ...
}
```

## Migration Guide

### Step 1: Update Direct Entity Calls
If you have code directly calling entity methods:
```java
// Before
travelRequest.submit();
receipt.validate();

// After
travelRequest.updateStatus(TravelRequestStatus.SUBMITTED, userId);
receipt.validate(userId);
```

### Step 2: Update Service Calls
```java
// Before
travelRequestService.submitTravelRequest(id);
receiptService.validateReceipt(receiptId);

// After
Long userId = getCurrentUserId(); // Implement this
travelRequestService.submitTravelRequest(id, userId);
receiptService.validateReceipt(receiptId, userId);
```

### Step 3: Implement User ID Extraction (TODO)
Replace hardcoded `userId = 1L` in controllers:

```java
// Option 1: Custom UserDetails
private Long extractUserId(User user) {
    if (user instanceof CustomUserDetails customUser) {
        return customUser.getUserId();
    }
    throw new IllegalStateException("Cannot extract user ID");
}

// Option 2: From username
private Long extractUserId(User user) {
    return Long.parseLong(user.getUsername());
}

// Option 3: Database lookup
private Long extractUserId(User user) {
    return userRepository.findByUsername(user.getUsername())
        .orElseThrow()
        .getId();
}
```

### Step 4: Test Email Notifications
Email notifications are now **async**. Verify:
1. ✅ Emails are sent asynchronously (check logs)
2. ✅ Email failures don't affect business operations
3. ✅ Event listeners are executed in separate transactions

**Example Log Output:**
```
2025-11-07 14:30:00 INFO  Processing TravelRequestStatusChangedEvent: DRAFT -> SUBMITTED
2025-11-07 14:30:01 INFO  Email notification sent for TravelRequest 123
```

## Architecture Changes

### Before (Tight Coupling)
```
Controller → Service (Business + Email) → Repository
                ↓
          EmailService
```

**Problems:**
- ❌ Business logic mixed with email logic
- ❌ Email failures break business transactions
- ❌ Hard to test
- ❌ Synchronous email sending

### After (Event-Driven DDD)
```
Controller → Service → Domain Model (Events) → Repository
                            ↓
                     Event Publishing
                            ↓
                    Event Listeners → EmailService
```

**Benefits:**
- ✅ Business logic separated from side-effects
- ✅ Email failures don't affect business transactions
- ✅ Easy to test (mock event publisher)
- ✅ Asynchronous email sending
- ✅ Audit trail (userId in events)

## Benefits

### 1. Decoupling
Email logic is completely separated from business logic via domain events.

### 2. Resilience
Email failures don't affect core business operations - logged but not thrown.

### 3. Performance
Async processing prevents email delays from blocking user requests.

### 4. Audit Trail
User IDs tracked for all status changes in domain events.

### 5. Domain Events
Foundation for future event-driven features (webhooks, notifications, analytics).

### 6. Testability
Business logic can be tested without email service dependencies.

### 7. Rich Domain Models
Business rules enforced in domain layer, not application services.

## DDD Principles Applied

✅ **Aggregate Root** - TravelRequest and Receipt extend AbstractAggregateRoot
✅ **Domain Events** - Status changes publish events
✅ **Rich Domain Model** - Business logic in entities (updateStatus, validate, reject)
✅ **Value Objects** - DateRange, Money (already existed)
✅ **Domain Exceptions** - InvalidStatusTransitionException
✅ **Application Services** - Thin orchestrators
✅ **Event Listeners** - Side-effects decoupled
✅ **Anti-Corruption Layer** - DTOs protect domain

## Next Steps

### Immediate (Required)
1. **Implement User ID Extraction**
   - Replace `userId = 1L` with actual authentication
   - Add Spring Security integration

### Short-term
2. **Add Integration Tests**
   - Test event publishing
   - Test email listener execution
   - Test async behavior

3. **Add Email Retry Mechanism**
   - Handle transient failures
   - Dead letter queue for failed emails

### Long-term
4. **Event Sourcing**
   - Store all domain events for complete audit trail
   - Replay events for debugging

5. **Complete DDD Refactoring**
   - Apply same patterns to other features
   - Refactor Employee to Aggregate Root
   - Implement CQRS with Use Cases
   - Add Specification Pattern for queries

6. **Archival Feature**
   - Implement using same DDD patterns
   - Archive domain events
   - Retention period value objects

## Testing Email Notifications

### Manual Testing
1. Start application: `mvn spring-boot:run`
2. Submit travel request: `POST /api/travel-requests/1/submit`
3. Check logs for:
   ```
   INFO  Processing TravelRequestStatusChangedEvent: DRAFT -> SUBMITTED
   INFO  Email notification sent for TravelRequest 1
   ```
4. Validate receipt: `POST /api/receipts/1/validate`
5. Check logs for receipt email

### Automated Testing (TODO)
```java
@Test
void shouldPublishEventOnStatusChange() {
    // Given
    TravelRequest request = new TravelRequest(...);
    
    // When
    request.updateStatus(TravelRequestStatus.SUBMITTED, 1L);
    
    // Then
    assertThat(request.getDomainEvents())
        .hasSize(1)
        .first()
        .isInstanceOf(TravelRequestStatusChangedEvent.class);
}
```

## Rollback Plan

If issues occur, rollback commits in reverse order:
```bash
git revert HEAD~14..HEAD  # Reverts last 15 commits
```

Or cherry-pick specific commits to keep:
```bash
git cherry-pick <commit-hash>  # Keep specific changes
```

## Questions?

Contact the development team or refer to:
- Domain-Driven Design by Eric Evans
- Implementing Domain-Driven Design by Vaughn Vernon
- Spring Data documentation on AbstractAggregateRoot

