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

  private val feeder = Iterator.continually(Map(
    "keyword" -> keywords(Random.nextInt(keywords.size))
  ))

  private val dbSearch = exec(
    http("db-search")
      .get("/api/posts/search")
      .queryParam("keyword", "${keyword}")
      .check(status.is(200))
  )

  private val esSearch = exec(
    http("es-search")
      .get("/api/v1/posts/elasticsearch")
      .queryParam("keyword", "${keyword}")
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

  setUp(
    scenarioBuilder.inject(
      rampUsers(warmupUsers) during 30.seconds,
      constantUsersPerSec(targetUsers.toDouble) during holdDuration.seconds
    )
  ).protocols(httpProtocol)
}
