package forex.services.rates

import java.time.OffsetDateTime

import cats.data.NonEmptyList
import cats.effect.Sync
import cats.syntax.applicativeError._
import cats.syntax.functor._
import cats.syntax.show._
import forex.config.OneFrameApiConfig
import forex.domain.{Currency, Price, Rate, Timestamp}
import forex.services.rates.OneFrameApiModule.OneFrameExchangeRate
import forex.services.rates.errors.Error.OneFrameApiError
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import org.http4s.circe.CirceInstances
import org.http4s.client.Client
import org.http4s.{EntityDecoder, Header, Headers, Request, Uri}

private[rates] object OneFrameApiModule {

  private implicit val oneFrameExchangeRateDecoder: Decoder[OneFrameExchangeRate] =
    deriveDecoder[OneFrameExchangeRate]

  private case class OneFrameExchangeRate(from: String,
                                          to: String,
                                          price: Double,
                                          time_stamp: String)
}

private[rates] class OneFrameApiModule[F[_]: Sync](config: OneFrameApiConfig,
                                                   httpClient: Client[F]) extends CirceInstances {

  private implicit val entityDecoder: EntityDecoder[F, List[OneFrameExchangeRate]] =
    accumulatingJsonOf

  private def requestUri(currencies: NonEmptyList[Rate.Currencies]): Uri =
    config.url.withQueryParam("pair", currencies.map(c => s"${c.from.show}${c.to.show}").toList)

  private def request(currencies: NonEmptyList[Rate.Currencies]): Request[F] =
    Request[F](uri = requestUri(currencies), headers = Headers.of(Header("token", config.accessToken)))

  def rates(currencies: NonEmptyList[Rate.Currencies]): F[Either[OneFrameApiError, Map[Rate.Currencies, Rate]]] =
    httpClient
      .expect[List[OneFrameExchangeRate]](request(currencies))
      .map[Either[OneFrameApiError, Map[Rate.Currencies, Rate]]] { rates =>
        val result = rates.map { rate =>
          val from = Currency.namesToValuesMap(rate.from)
          val to = Currency.namesToValuesMap(rate.to)
          val price = Price(BigDecimal(rate.price))
          val timestamp = Timestamp(OffsetDateTime.parse(rate.time_stamp))
          val currencies = Rate.Currencies(from, to)
          currencies -> Rate(currencies, price, timestamp)
        }.toMap
        Right(result)
      }
      .handleError(e => Left(OneFrameApiError(e.getMessage)))
}
