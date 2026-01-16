# 🚀 Gatling Load Tests - Quick Start Guide

## ⚡ Schnellstart (3 Schritte)

### 1️⃣ Backend starten
```bash
cd /Users/ksa/VSCode/VibeSoftwareEngineering/ConcertComparison/backend
./mvnw spring-boot:run
```

**Warte bis du siehst:**
```
Started ConcertComparisonApplication in X.XXX seconds
```

### 2️⃣ Smoke Test ausführen (10 Sekunden)
```bash
# In neuem Terminal-Fenster:
./mvnw gatling:test -Dgatling.simulationClass=com.concertcomparison.loadtest.BasicSmokeTest
```

**Erwartetes Ergebnis:**
```
✅ Smoke Test abgeschlossen - System ist betriebsbereit!
================================================================================
Global: mean response time OK
Global: percentage of successful requests OK
================================================================================
Simulation com.concertcomparison.loadtest.BasicSmokeTest completed in X seconds
Reports generated in 0s.
```

### 3️⃣ HTML-Report öffnen
```bash
# macOS:
open target/gatling/basicsmoketest-*/index.html

# Linux:
xdg-open target/gatling/basicsmoketest-*/index.html

# Windows:
start target/gatling/basicsmoketest-*/index.html
```

---

## 📊 Alle Load Tests

### 🟢 BasicSmokeTest (10s) - CI/CD Ready
```bash
./mvnw gatling:test -Dgatling.simulationClass=com.concertcomparison.loadtest.BasicSmokeTest
```
- 1 Health Check
- 1 Availability Check
- 10 concurrent Holds
- **Use Case:** Schnelle Validierung vor Deployment

### 🟡 SeatHoldLoadTest (2min) - Realistisch
```bash
./mvnw gatling:test -Dgatling.simulationClass=com.concertcomparison.loadtest.SeatHoldLoadTest
```
- 100 User → 1 Seat (Race Condition)
- 500 User Burst Traffic (Verkaufsstart)
- 50 User normale Last
- **Use Case:** Performance-Tuning, Capacity Planning

### 🔴 ConcurrencyStressTest (3min) - Extreme Last
```bash
./mvnw gatling:test -Dgatling.simulationClass=com.concertcomparison.loadtest.ConcurrencyStressTest
```
- 1000 User gleichzeitig
- 10.000 Requests über 60s
- Spike: 100 → 1000 User in 1s
- **Use Case:** Breaking-Point Testing

### ⚡ Alle Tests nacheinander
```bash
./mvnw gatling:test
```
Führt alle 3 Tests nacheinander aus (~5 Minuten).

---

## 🎯 Erwartete Ergebnisse

### Race Condition Test (100 User → 1 Seat)
```
✅ 1x HTTP 200 OK    (Gewinner)
✅ 99x HTTP 409 CONFLICT (Verlierer - kein Fehler!)
❌ 0x Failures
```

**WICHTIG:** HTTP 409 ist **KEIN FEHLER**, sondern korrektes Concurrency-Verhalten!

### Performance Targets
| Metrik | Target | Test |
|--------|--------|------|
| Mean Response Time | < 800ms | Alle |
| 95th Percentile | < 1000ms | Normal Load |
| 99th Percentile | < 2000ms | Burst Traffic |
| Erfolgsrate | ≥ 95% | Normal Load |
| Max Response Time | < 5000ms | Stress Test |

---

## 🔧 Troubleshooting

### ❌ Problem: "Connection refused"
**Ursache:** Backend nicht gestartet
```bash
./mvnw spring-boot:run
```

### ❌ Problem: "Compilation failed: not found: value http"
**Ursache:** Gatling Dependencies fehlen
```bash
./mvnw clean install
```

### ❌ Problem: Alle Requests = 404
**Ursache:** Backend läuft auf falschem Port
```bash
# Prüfe:
curl http://localhost:8080/actuator/health

# Falls Port anders, ändere in Test:
# .baseUrl("http://localhost:DEIN_PORT")
```

### ❌ Problem: "OutOfMemoryError"
**Lösung:** Mehr RAM für Maven
```bash
export MAVEN_OPTS="-Xmx4g"
./mvnw gatling:test
```

---

## 📈 Report verstehen

### Wichtigste Metriken im HTML-Report

#### 1. Response Time Distribution
- **Grün:** < 800ms ✅
- **Gelb:** 800ms - 1200ms ⚠️
- **Rot:** > 1200ms ❌

#### 2. Requests per Second
- Zeigt Last-Profil über Zeit
- Spikes sollten ohne Fehler durchlaufen

#### 3. Response Time Percentiles
- **50th (Median):** Typische Response Time
- **95th:** Fast alle Nutzer (95%)
- **99th:** Worst Case (99%)

#### 4. Status Codes
- **200 OK:** Erfolgreich reserviert
- **409 CONFLICT:** Seat schon reserviert (OK!)
- **500/503:** Echte Fehler (NICHT OK!)

---

## 🚦 CI/CD Integration

### GitHub Actions (`.github/workflows/load-test.yml`)
```yaml
name: Load Tests
on: [push, pull_request]

jobs:
  smoke-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '21'
      
      - name: Start Backend
        run: |
          ./mvnw spring-boot:run &
          sleep 30  # Warte auf Backend-Start
      
      - name: Run Smoke Test
        run: ./mvnw gatling:test -Dgatling.simulationClass=com.concertcomparison.loadtest.BasicSmokeTest
      
      - name: Upload Report
        uses: actions/upload-artifact@v3
        with:
          name: gatling-report
          path: target/gatling/
```

---

## 🎓 Nächste Schritte

### 1. Baseline etablieren
```bash
# Führe 3x aus, nimm Durchschnitt:
./mvnw gatling:test -Dgatling.simulationClass=com.concertcomparison.loadtest.SeatHoldLoadTest
```

### 2. Performance optimieren
- Connection Pool erhöhen (`application.properties`)
- Caching für Availability-Checks
- Pessimistic Locking für Hot Seats

### 3. Production-Tests
- Führe auf Production-ähnlicher Hardware aus
- Nutze echte PostgreSQL (nicht H2)
- Simuliere geografisch verteilte User

### 4. Monitoring einrichten
- Prometheus + Grafana
- APM (New Relic, Datadog, Elastic APM)
- Custom Metrics für Business-KPIs

---

## 📚 Weiterführende Dokumentation

- [Gatling Dokumentation](https://gatling.io/docs/gatling/)
- [Performance Testing Best Practices](https://gatling.io/load-testing-best-practices/)
- Siehe `README.md` für detaillierte Erklärungen

---

## ✅ Checkliste für Production-Readiness

- [ ] Smoke Test läuft erfolgreich durch
- [ ] Load Test validiert 95th Percentile < 1s
- [ ] Stress Test zeigt keine Crashes
- [ ] Race Condition Test: Nur 1x HTTP 200
- [ ] HTML-Reports zeigen grüne Metriken
- [ ] Tests in CI/CD Pipeline integriert
- [ ] Baseline-Metriken dokumentiert
- [ ] Monitoring & Alerting konfiguriert

---

**Viel Erfolg beim Load Testing! 🚀**
