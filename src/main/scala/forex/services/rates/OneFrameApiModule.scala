package forex.services.rates

import java.time.OffsetDateTime

import cats.data.NonEmptyList
import cats.effect.Sync
import cats.syntax.functor._
import cats.syntax.show._
import forex.config.OneFrameApiConfig
import forex.domain.{Currency, Price, Rate, Timestamp}
import forex.services.rates.OneFrameApiModule.OneFrameExchangeRate
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import org.http4s.circe.CirceInstances
import org.http4s.client.Client
import org.http4s.{EntityDecoder, Header, Headers, Request, Uri}

private[rates] object OneFrameApiModule {

  implicit val currencyDecoder: Decoder[Currency] =
    Decoder[String].emap {
      case s if Currency.namesToValuesMap.contains(s) =>
        Right(Currency.namesToValuesMap(s))

      case s =>
        Left(s"Not a valid currency value: $s")
    }

  implicit val oneFrameRateDecoder: Decoder[OneFrameExchangeRate] =
    deriveDecoder[OneFrameExchangeRate]

  private case class OneFrameExchangeRate(from: Currency,
                                          to: Currency,
                                          price: BigDecimal,
                                          time_stamp: OffsetDateTime)
}

private[rates] class OneFrameApiModule[F[_]: Sync](config: OneFrameApiConfig,
                                                   httpClient: Client[F]) extends CirceInstances {

  private implicit val entityDecoder: EntityDecoder[F, List[OneFrameExchangeRate]] =
    accumulatingJsonOf

  private def requestUri(currencies: NonEmptyList[Rate.Currencies]): Uri =
    config.url.withQueryParam("pair", currencies.map(c => s"${c.from.show}${c.to.show}").toList)

  private def request(currencies: NonEmptyList[Rate.Currencies]): Request[F] =
    Request[F](uri = requestUri(currencies), headers = Headers.of(Header("token", config.accessToken.value)))

  def getRates(currencies: NonEmptyList[Rate.Currencies]): F[Map[Rate.Currencies, Rate]] =
    httpClient
      .expect[List[OneFrameExchangeRate]](request(currencies))
      .map { rates =>
        rates.map { rate =>
          val currencies = Rate.Currencies(rate.from, rate.to)
          currencies -> Rate(currencies, Price(rate.price), Timestamp(rate.time_stamp))
        }.toMap
      }
}
