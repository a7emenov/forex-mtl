package forex.api

import cats.effect.IO
import forex.api.rates.RatesApi
import forex.domain.Rate
import forex.services.RatesService
import forex.services.rates.errors.Error.RateNotAvailable
import forex.{ programs, BaseSpec }
import io.circe.parser.decode
import io.circe.{ Json, JsonObject }
import org.http4s.Method._
import util.Generators._
import org.http4s._
import org.http4s.client.dsl.io._
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME

class RatesApiSpec extends BaseSpec {

  behavior of "Rates API"

  it should "return successful responses when values are present in the cache" in {
    forAll(rateMap) { rates =>
      withApi(rates) { api =>
        rates.foreach {
          case (key, value) =>
            val request =
              GET(Uri.unsafeFromString(s"/rates?from=${key.from.entryName}&to=${key.to.entryName}")).unsafeRunSync()
            checkResponse(
              expectedStatus = Status.Ok,
              expectedFields = JsonObject(
                "from" -> Json.fromString(key.from.entryName),
                "to" -> Json.fromString(key.to.entryName),
                "price" -> Json.fromBigDecimal(value.price.value),
                "timestamp" -> Json.fromString(value.timestamp.value.format(ISO_OFFSET_DATE_TIME))
              ),
              responseF = api.routes(request).value
            )
        }
      }
    }
  }

  it should "return successful responses when values are not present in the cache" in {
    forAll(rateMap, rateMap) { (rates1, rates2) =>
      withApi(rates1) { api =>
        rates2.keys.filterNot(rates1.contains).foreach { key =>
          val request =
            GET(Uri.unsafeFromString(s"/rates?from=${key.from.entryName}&to=${key.to.entryName}")).unsafeRunSync()
          checkResponse(
            expectedStatus = Status.NotFound,
            expectedFields = JsonObject(
              "from" -> Json.fromString(key.from.entryName),
              "to" -> Json.fromString(key.to.entryName),
              "message" -> Json
                .fromString(s"Rate is not available for currencies: ${key.from.entryName} to ${key.to.entryName}")
            ),
            responseF = api.routes(request).value
          )
        }
      }
    }
  }

  private def dummyRateService(values: Map[Rate.Currencies, Rate]): RatesService[IO] =
    (currencies: Rate.Currencies) => IO.pure(values.get(currencies).toRight(RateNotAvailable(currencies)))

  private def withApi(values: Map[Rate.Currencies, Rate])(testCode: RatesApi[IO] => Any): Any =
    testCode(new RatesApi[IO](programs.RatesProgram(dummyRateService(values))))

  private def checkResponse(expectedStatus: Status,
                            expectedFields: JsonObject,
                            responseF: IO[Option[Response[IO]]]): Unit = {
    val response = responseF.unsafeRunSync()
    response shouldBe a[Some[_]]
    response.get.status shouldBe expectedStatus
    val json = decode[JsonObject](response.get.bodyAsText.compile.string.unsafeRunSync())
    json shouldBe a[Right[_, _]]
    expectedFields.keys.foreach { key =>
      json.right.get(key) shouldBe expectedFields(key)
    }
  }
}
