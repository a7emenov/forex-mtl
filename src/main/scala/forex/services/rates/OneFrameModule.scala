package forex.services.rates

import java.time.OffsetDateTime

import cats.effect._
import cats.syntax.functor._
import cats.syntax.show._
import forex.config.OneFrameConfig
import forex.domain.{Currency, Price, Rate, Timestamp}
import forex.services.rates.OneFrameModule.OneFrameExchangeRate
import forex.services.rates.errors.Error.OneFrameLookupFailed
import io.circe._
import io.circe.generic.semiauto._
import org.http4s.circe.CirceInstances
import org.http4s.client.Client
import org.http4s.{EntityDecoder, Header, Headers, Request, Uri}
import cats.syntax.applicativeError._

private[rates] object OneFrameModule {

  private implicit val oneFrameExchangeRateDecoder: Decoder[OneFrameExchangeRate] =
    deriveDecoder[OneFrameExchangeRate]

  private case class OneFrameExchangeRate(from: String,
                                          to: String,
                                          price: Double,
                                          time_stamp: String)
}

private[rates] class OneFrameModule[F[_]: Sync](config: OneFrameConfig,
                                                httpClient: Client[F]) extends Algebra[F] with CirceInstances {

  private implicit val entityDecoder: EntityDecoder[F, List[OneFrameExchangeRate]] =
    accumulatingJsonOf

  private val baseUrl = Uri.unsafeFromString(config.url.toExternalForm)

  private def requestUri(from: Currency, to: Currency): Uri =
    baseUrl.withQueryParam("pair", s"${from.show}${to.show}")

  private def request(from: Currency, to: Currency): Request[F] =
    Request[F](uri = requestUri(from, to), headers = Headers.of(Header("token", config.accessToken)))

  override def get(from: Currency, to: Currency): F[Either[errors.Error, Rate]] =
    httpClient
      .expect[List[OneFrameExchangeRate]](request(from, to))
      .map[Either[errors.Error, Rate]] {
        case head :: Nil =>
          Right(Rate(from, to, Price(BigDecimal(head.price)), Timestamp(OffsetDateTime.parse(head.time_stamp))))

        case ls =>
          Left(OneFrameLookupFailed(s"Invalid response: expected 1 entry, got ${ls.length}"))
      }
      .handleError(e => Left(OneFrameLookupFailed(e.getMessage)))
}
