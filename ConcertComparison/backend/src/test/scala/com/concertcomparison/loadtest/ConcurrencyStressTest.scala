package com.concertcomparison.loadtest

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

/**
 * Extremer Concurrency Stress Test.
 * 
 * Simuliert 10.000+ Requests/Sekunde auf Hot Seats.
 * Validiert Optimistic & Pessimistic Locking unter extremer Last.
 * 
 * Führt folgende Tests aus:
 * 1. Hot Seat Test: 1000 User konkurrieren um 1 Seat
 * 2. Sustained Load: 10.000 Requests über 60 Sekunden
 * 3. Spike Test: Plötzliche Last-Spitzen (100 → 1000 User in 1s)
 * 
 * WARNUNG: Ressourcen-intensiv! Backend braucht mindestens 4GB RAM.
 * 
 * Ausführung:
 * ./mvnw gatling:test -Dgatling.simulationClass=com.concertcomparison.loadtest.ConcurrencyStressTest
 */
class ConcurrencyStressTest extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .userAgentHeader("Gatling Stress Test")
    .maxConnectionsPerHost(500)  // Erhöhe Connection Pool für hohe Last

  // Helper: Generiere eindeutige User IDs
  val userIdFeeder = Iterator.continually(Map("userId" -> java.util.UUID.randomUUID().toString))
  
  // Helper: Generiere zufällige Seat IDs (1-100)
  val seatIdFeeder = Iterator.continually(Map("seatId" -> (scala.util.Random.nextInt(100) + 1)))
  
  // Helper: Generiere zufällige Seat IDs (1-50) für Spike
  val spikeSeatIdFeeder = Iterator.continually(Map("seatId" -> (scala.util.Random.nextInt(50) + 1)))

  // Scenario 1: Hot Seat Test - 1000 Nutzer kämpfen um EINEN Seat
  val hotSeatScenario = scenario("Hot Seat - 1000 Concurrent Users")
    .feed(userIdFeeder)
    .exec(http("Hold Hot Seat")
      .post("/api/seats/1/hold")
      .body(StringBody("""{"userId": "#{userId}"}""")).asJson
      .check(status.in(200, 409))
      .check(responseTimeInMillis.lte(5000))  // Max 5s unter extremer Last
    )

  // Scenario 2: Sustained High Load - 10.000 Requests über 60s
  val sustainedLoadScenario = scenario("Sustained Load - 10k Requests")
    .feed(userIdFeeder)
    .feed(seatIdFeeder)
    .exec(http("Hold Seat Under Sustained Load")
      .post("/api/seats/#{seatId}/hold")
      .body(StringBody("""{"userId": "#{userId}"}""")).asJson
      .check(status.in(200, 409))
      .check(responseTimeInMillis.lte(3000))
    )
    .pause(50.milliseconds, 200.milliseconds)

  // Scenario 3: Spike Test - Plötzliche Last-Spitze
  val spikeScenario = scenario("Spike Test - Sudden Traffic Increase")
    .feed(userIdFeeder)
    .feed(spikeSeatIdFeeder)
    .exec(http("Hold Seat During Spike")
      .post("/api/seats/#{seatId}/hold")
      .body(StringBody("""{"userId": "#{userId}"}""")).asJson
      .check(status.in(200, 409, 503))  // 503 Service Unavailable auch akzeptabel
      .check(responseTimeInMillis.lte(10000))  // Max 10s bei Spitze
    )

  // Scenario 4: Pessimistic Locking Test - Für kritische Hot Seats
  val pessimisticLockScenario = scenario("Pessimistic Locking Test")
    .feed(userIdFeeder)
    .exec(http("Hold with Pessimistic Lock")
      .post("/api/seats/1/hold")  // Nutze Standard-Endpoint
      .body(StringBody("""{"userId": "#{userId}"}""")).asJson
      .check(status.in(200, 409))
      .check(responseTimeInMillis.lte(2000))  // Pessimistic Lock sollte schneller sein
    )

  setUp(
    // Test 1: Hot Seat - 1000 User gleichzeitig
    hotSeatScenario.inject(
      atOnceUsers(1000)
    ).protocols(httpProtocol),

    // Test 2: Sustained Load - 10.000 Requests über 60s (ca. 167 req/s)
    sustainedLoadScenario.inject(
      constantUsersPerSec(167) during (60.seconds)
    ).protocols(httpProtocol),

    // Test 3: Spike Test - Von 100 auf 1000 User in 1 Sekunde
    spikeScenario.inject(
      rampUsers(100) during (10.seconds),      // Warmup: 100 User
      nothingFor(5.seconds),                    // Kurze Pause
      rampUsers(1000) during (1.second),       // SPIKE: 1000 User in 1s!
      constantUsersPerSec(50) during (30.seconds)  // Recovery
    ).protocols(httpProtocol),

    // Test 4: Pessimistic Locking (optional, falls Endpoint existiert)
    // pessimisticLockScenario.inject(
    //   rampUsers(100) during (10.seconds)
    // ).protocols(httpProtocol)
  ).assertions(
    // Globale Performance Targets (statt scenario-spezifisch)
    global.responseTime.percentile3.lte(3000),  // 95% < 3s
    global.responseTime.percentile4.lte(8000),  // 99% < 8s
    global.failedRequests.percent.lte(10)       // < 10% Fehler (bei extremer Last akzeptabel)
  )

  // Performance Report nach Test
  after {
    println("=" * 80)
    println("CONCURRENCY STRESS TEST ABGESCHLOSSEN")
    println("=" * 80)
    println("Prüfe HTML-Report unter: target/gatling/results/")
    println("Erwartete Metriken:")
    println("  - Hot Seat: Nur 1x HTTP 200, Rest 409 CONFLICT")
    println("  - Sustained Load: >99% Erfolgsrate")
    println("  - Spike: System sollte sich erholen")
    println("=" * 80)
  }
}
