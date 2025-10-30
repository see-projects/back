package dooya.see.search

import scala.concurrent.duration._
import scala.util.Random

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class PostSearchSimulation extends Simulation {
  private val baseUrl = Option(System.getProperty("benchmark.baseUrl")).getOrElse("http://localhost:8080")
  private val keywords = Option(System.getProperty("benchmark.keywords"))
    .map(_.split(",").toList)
    .getOrElse(List("Spring", "Java", "Elasticsearch", "Query", "Optimization"))

  private val httpProtocol = http
    .baseUrl(baseUrl)
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  private val feeder = Iterator.continually {
    val baseKeyword = keywords(Random.nextInt(keywords.size))
    Map(
      "hotKeyword" -> baseKeyword,
      "coldKeyword" -> s"$baseKeyword-${System.nanoTime()}"
    )
  }

  private val dbSearch = exec(
    http("db-search")
      .get("/api/posts/search")
      .queryParam("keyword", "${hotKeyword}")
      .check(status.is(200))
  )

  private val esSearch = exec(
    http("es-cold-search")
      .get("/api/v1/posts/elasticsearch")
      .queryParam("keyword", "${coldKeyword}")
      .check(status.is(200))
  )

  private val esCacheWarm = exec(
    http("es-cache-warm")
      .get("/api/v1/posts/elasticsearch")
      .queryParam("keyword", "${hotKeyword}")
      .check(status.is(200))
      .silent
  )

  private val esCacheHit = exec(
    http("es-cache-hit")
      .get("/api/v1/posts/elasticsearch")
      .queryParam("keyword", "${hotKeyword}")
      .check(status.is(200))
  )

  private val warmupUsers = Integer.getInteger("benchmark.warmupUsers", 10)
  private val targetUsers = Integer.getInteger("benchmark.targetUsers", 50)
  private val holdDuration = Integer.getInteger("benchmark.holdSeconds", 60)

  private val scenarioBuilder = scenario("post-search-comparison")
    .feed(feeder)
    .exec(dbSearch)
    .pause(500.milliseconds, 2.seconds)
    .exec(esSearch)
    .pause(200.milliseconds, 500.milliseconds)
    .exec(esCacheWarm)
    .pause(100.milliseconds)
    .exec(esCacheHit)

  setUp(
    scenarioBuilder.inject(
      rampUsers(warmupUsers) during 30.seconds,
      constantUsersPerSec(targetUsers.toDouble) during holdDuration.seconds
    )
  ).protocols(httpProtocol)
}
