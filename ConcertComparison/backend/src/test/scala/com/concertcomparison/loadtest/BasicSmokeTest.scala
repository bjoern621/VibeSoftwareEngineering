package com.concertcomparison.loadtest

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

/**
 * Basic Smoke Test für schnelle Validierung.
 * 
 * Testet grundlegende Funktionalität mit geringer Last:
 * - Health Check
 * - Seat Availability
 * - Single Hold Request
 * - Concurrent Holds (10 User)
 * 
 * Perfekt für:
 * - CI/CD Pipeline (schnell, < 30s)
 * - Entwicklungs-Checks
 * - Pre-Deployment Validation
 * 
 * Ausführung: ./mvnw gatling:test -Dgatling.simulationClass=com.concertcomparison.loadtest.BasicSmokeTest
 */
class BasicSmokeTest extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  // Test 1: Health Check
  val healthCheckScenario = scenario("Health Check")
    .exec(http("GET /actuator/health")
      .get("/actuator/health")
      .check(status.is(200))
      .check(jsonPath("$.status").is("UP"))
    )

  // Test 2: Seat Availability
  val availabilityScenario = scenario("Seat Availability")
    .exec(http("GET /api/events/1/seats")
      .get("/api/events/1/seats")
      .check(status.is(200))
      .check(jsonPath("$.seats").exists)
      .check(jsonPath("$.availabilityByCategory").exists)
    )

  // Test 3: Single Hold Request
  val singleHoldScenario = scenario("Single Hold Request")
    .exec(http("POST /api/seats/5/hold")
      .post("/api/seats/5/hold")
      .body(StringBody("""{"userId": "smoke-test-user"}""")).asJson
      .check(status.in(200, 409))
    )

  // Test 4: Concurrent Holds - 10 User
  val userIdFeeder = Iterator.from(1).map(i => Map("counter" -> i))
  
  val concurrentHoldsScenario = scenario("Concurrent Holds - 10 Users")
    .feed(userIdFeeder)
    .exec(http("POST /api/seats/1/hold (concurrent)")
      .post("/api/seats/1/hold")
      .body(StringBody("""{"userId": "user-#{counter}"}""")).asJson
      .check(status.in(200, 409))
    )

  setUp(
    healthCheckScenario.inject(atOnceUsers(1)).protocols(httpProtocol),
    availabilityScenario.inject(
      nothingFor(1.second),
      atOnceUsers(1)
    ).protocols(httpProtocol),
    singleHoldScenario.inject(
      nothingFor(2.seconds),
      atOnceUsers(1)
    ).protocols(httpProtocol),
    concurrentHoldsScenario.inject(
      nothingFor(3.seconds),
      atOnceUsers(10)
    ).protocols(httpProtocol)
  ).assertions(
    global.responseTime.max.lte(2000),
    global.successfulRequests.percent.gte(90)
  )

  after {
    println("✅ Smoke Test abgeschlossen - System ist betriebsbereit!")
  }
}
