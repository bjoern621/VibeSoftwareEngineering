# Gatling Load Tests - Concert Comparison

## Übersicht

Dieses Verzeichnis enthält Gatling Load Tests für das Concert Comparison Backend.
Die Tests validieren Concurrency Control, Performance und Skalierbarkeit unter realistischer Last.

## Test-Szenarien

### 1. BasicSmokeTest.scala
**Zweck:** Schnelle Validierung der Grundfunktionalität
- Health Check
- Seat Availability Check
- Single Hold Request
- 10 concurrent Holds
- **Dauer:** ~10 Sekunden
- **Use Case:** CI/CD Pipeline, Pre-Deployment Checks

```bash
./mvnw gatling:test -Dgatling.simulationClass=com.concertcomparison.loadtest.BasicSmokeTest
```

### 2. SeatHoldLoadTest.scala
**Zweck:** Realistische Last-Simulation für Production
- **Race Condition Test:** 100 User auf denselben Seat
- **Normale Last:** 50 User über 30 Sekunden
- **Burst Traffic:** 500 User in 10 Sekunden (Verkaufsstart)
- **Read-Heavy:** 20 Availability-Checks/Sekunde
- **Dauer:** ~2 Minuten
- **Use Case:** Performance-Tuning, Capacity Planning

```bash
./mvnw gatling:test -Dgatling.simulationClass=com.concertcomparison.loadtest.SeatHoldLoadTest
```

### 3. ConcurrencyStressTest.scala
**Zweck:** Extreme Last für Stress Testing
- **Hot Seat:** 1000 User gleichzeitig auf 1 Seat
- **Sustained Load:** 10.000 Requests über 60 Sekunden
- **Spike Test:** 100 → 1000 User in 1 Sekunde
- **Dauer:** ~3 Minuten
- **Use Case:** Worst-Case Validation, Breaking-Point Testing

```bash
./mvnw gatling:test -Dgatling.simulationClass=com.concertcomparison.loadtest.ConcurrencyStressTest
```

## Voraussetzungen

### 1. Backend starten
```bash
cd /Users/ksa/VSCode/VibeSoftwareEngineering/ConcertComparison/backend
./mvnw spring-boot:run
```

Backend muss auf `http://localhost:8080` laufen.

### 2. Testdaten vorbereiten
Das Backend sollte mindestens folgende Testdaten haben:
- Concert mit ID=1
- 100 Seats (IDs 1-100) für Concert 1

Diese werden automatisch durch `DataLoader.java` erstellt.

## Tests ausführen

### Alle Tests ausführen
```bash
./mvnw gatling:test
```

### Einzelnen Test ausführen
```bash
./mvnw gatling:test -Dgatling.simulationClass=com.concertcomparison.loadtest.BasicSmokeTest
```

### Test-Report öffnen
Nach dem Test wird automatisch ein HTML-Report generiert:
```
target/gatling/<test-name>-<timestamp>/index.html
```

Öffne den Report im Browser:
```bash
open target/gatling/*/index.html  # macOS
xdg-open target/gatling/*/index.html  # Linux
```

## Performance-Metriken

Die Tests validieren folgende KPIs:

### Response Time
- **Durchschnitt:** < 800ms (global)
- **95th Percentile:** < 1000ms (normale Last)
- **99th Percentile:** < 2000ms (Burst Traffic)

### Erfolgsrate
- **Normale Last:** ≥ 95% HTTP 200
- **Concurrency Test:** 1x HTTP 200, Rest HTTP 409 (kein Fehler!)
- **Globale Fehlerrate:** < 1% (ohne 409)

### Throughput
- **Normale Last:** 10-20 Requests/Sekunde
- **Burst Traffic:** 50-100 Requests/Sekunde
- **Sustained Load:** 167 Requests/Sekunde

## Expected Results

### Race Condition Test (100 User → 1 Seat)
✅ **Erwartetes Verhalten:**
- **1x HTTP 200 OK** (Seat erfolgreich reserviert)
- **99x HTTP 409 CONFLICT** (Seat bereits reserviert)
- **0x Failures** (409 ist kein Fehler, sondern korrektes Verhalten!)

### Normal Load Test
✅ **Erwartetes Verhalten:**
- Response Time < 1 Sekunde
- ≥95% Erfolgsrate
- Keine Timeout-Fehler

### Stress Test (1000+ concurrent users)
✅ **Erwartetes Verhalten:**
- System bleibt stabil (kein Crash)
- Response Time degradiert gracefully (< 5 Sekunden)
- Optimistic Locking Konflikte werden korrekt behandelt

## Troubleshooting

### Problem: "Connection refused"
**Lösung:** Backend ist nicht gestartet
```bash
./mvnw spring-boot:run
```

### Problem: "Too many open files"
**Lösung:** Erhöhe System-Limits (macOS/Linux)
```bash
ulimit -n 10000
```

### Problem: "OutOfMemoryError"
**Lösung:** Erhöhe JVM Heap Size
```bash
export MAVEN_OPTS="-Xmx4g"
./mvnw gatling:test
```

### Problem: Alle Requests schlagen fehl
**Lösung:** Prüfe, ob Testdaten vorhanden sind
```bash
curl http://localhost:8080/api/seats/availability?concertId=1
```

## CI/CD Integration

### GitHub Actions Beispiel
```yaml
name: Load Tests

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  load-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
      - name: Start Backend
        run: ./mvnw spring-boot:run &
      - name: Wait for Backend
        run: |
          timeout 60 bash -c 'until curl -f http://localhost:8080/actuator/health; do sleep 2; done'
      - name: Run Smoke Test
        run: ./mvnw gatling:test -Dgatling.simulationClass=com.concertcomparison.loadtest.BasicSmokeTest
      - name: Upload Gatling Report
        uses: actions/upload-artifact@v3
        with:
          name: gatling-report
          path: target/gatling/
```

## Nächste Schritte

1. **Baseline etablieren:** Führe Tests auf Production-Hardware aus
2. **Performance-Tuning:** Optimiere basierend auf Report-Metriken
3. **Monitoring:** Integriere mit Prometheus/Grafana
4. **Alerts:** Setze Thresholds für Response Time & Error Rate
5. **Regression Tests:** Füge zu CI/CD Pipeline hinzu

## Performance-Optimierungen (falls nötig)

### Backend-Optimierungen
- Connection Pool erhöhen (`spring.datasource.hikari.maximum-pool-size=50`)
- Pessimistic Locking für Hot Seats aktivieren
- Caching für Availability-Checks (Redis/Caffeine)
- Read Replicas für Read-Heavy Scenarios

### JVM-Tuning
```bash
java -Xms2g -Xmx4g -XX:+UseG1GC -jar app.jar
```

### Database-Tuning
- Indizes auf `seat_id`, `concert_id`, `status`
- VACUUM/ANALYZE für PostgreSQL
- Connection Pooling optimieren

## Kontakt

Bei Fragen zu den Load Tests:
- Siehe CONCERT COMPARISON Dokumentation
- Check GitHub Issues
- Review Gatling Dokumentation: https://gatling.io/docs/
