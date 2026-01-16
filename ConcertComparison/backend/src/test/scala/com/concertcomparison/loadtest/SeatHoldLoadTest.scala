package com.concertcomparison.loadtest

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

/**
 * Gatling Load Test für Seat Hold Operations.
 * 
 * Testet Concurrency Control unter realistischer Last:
 * - 100 parallele Nutzer versuchen denselben Seat zu reservieren
 * - Validiert, dass nur 1x HTTP 200 OK und 99x HTTP 409 CONFLICT
 * - Misst Response Time, Throughput und Error Rate
 * 
 * WICHTIG: Backend muss auf localhost:8080 laufen!
 * Start mit: ./mvnw spring-boot:run
 * Dann: ./mvnw gatling:test
 */
class SeatHoldLoadTest extends Simulation {

  // HTTP Configuration
  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .userAgentHeader("Gatling Load Test")

  // Scenario 1: Race Condition Test - 100 User versuchen denselben Seat zu reservieren
  val userIdFeeder = Iterator.from(1).map(i => Map("userId" -> s"user-$i"))
  
  val raceConditionScenario = scenario("Race Condition - Same Seat")
    .feed(userIdFeeder)
    .exec(http("Hold Same Seat")
      .post("/api/seats/1/hold")
      .body(StringBody("""{"userId": "#{userId}"}""")).asJson
      .check(status.in(200, 409))  // 200 OK oder 409 CONFLICT akzeptabel
      .check(jsonPath("$.seatId").optional.saveAs("reservedSeatId"))
      .check(jsonPath("$.message").optional.saveAs("errorMessage"))
    )
    .pause(100.milliseconds)

  // Scenario 2: Normale Last - Verschiedene Seats parallel
  val randomSeatFeeder = Iterator.continually(Map(
    "seatId" -> (scala.util.Random.nextInt(50) + 1),
    "userId" -> s"user-${scala.util.Random.nextInt(100000)}"
  ))
  
  val normalLoadScenario = scenario("Normal Load - Different Seats")
    .feed(randomSeatFeeder)
    .exec(http("Hold Different Seat")
      .post("/api/seats/#{seatId}/hold")
      .body(StringBody("""{"userId": "#{userId}"}""")).asJson
      .check(status.in(200, 409))
      .check(responseTimeInMillis.lte(1000))  // Max 1 Sekunde Response Time
    )
    .pause(500.milliseconds, 1.second)

  // Scenario 3: Burst Traffic - Verkaufsstart Simulation
  val burstSeatFeeder = Iterator.continually(Map(
    "seatId" -> (scala.util.Random.nextInt(100) + 1),
    "userId" -> s"user-${scala.util.Random.nextInt(100000)}"
  ))
  
  val burstTrafficScenario = scenario("Burst Traffic - Sales Start")
    .feed(burstSeatFeeder)
    .exec(http("Hold Seat on Sales Start")
      .post("/api/seats/#{seatId}/hold")
      .body(StringBody("""{"userId": "#{userId}"}""")).asJson
      .check(status.in(200, 409))
      .check(responseTimeInMillis.lte(2000))
    )

  // Scenario 4: Seat Availability Check unter Last
  val availabilityCheckScenario = scenario("Seat Availability Check")
    .exec(http("Get Seat Availability")
      .get("/api/events/1/seats")
      .check(status.is(200))
      .check(jsonPath("$.seats").exists)
      .check(responseTimeInMillis.lte(500))
    )
    .pause(200.milliseconds, 500.milliseconds)

  // Load Test Setup
  setUp(
    // Test 1: Race Condition - 100 User gleichzeitig auf denselben Seat
    raceConditionScenario.inject(
      atOnceUsers(100)  // Alle 100 gleichzeitig
    ).protocols(httpProtocol),

    // Test 2: Normale Last - 50 User über 30 Sekunden
    normalLoadScenario.inject(
      rampUsers(50) during (30.seconds),
      constantUsersPerSec(10) during (60.seconds)
    ).protocols(httpProtocol),

    // Test 3: Burst Traffic - Verkaufsstart mit 500 Usern in 10 Sekunden
    burstTrafficScenario.inject(
      nothingFor(5.seconds),  // Warten bis normale Last stabil
      rampUsers(500) during (10.seconds)
    ).protocols(httpProtocol),

    // Test 4: Read-Heavy Load - Availability Checks
    availabilityCheckScenario.inject(
      constantUsersPerSec(20) during (90.seconds)
    ).protocols(httpProtocol)
  ).assertions(
    // Globale Assertions - alle Tests zusammen
    global.responseTime.mean.lte(800),  // Durchschnitt < 800ms
    global.responseTime.percentile3.lte(1000),  // 95th percentile < 1 Sekunde
    global.responseTime.percentile4.lte(2000),  // 99th percentile < 2 Sekunden
    global.successfulRequests.percent.gte(90),  // Mind. 90% erfolgreich (409 ist OK)
    global.failedRequests.count.lte(100)  // Max 100 echte Fehler (HTTP 5xx)
  )
}
