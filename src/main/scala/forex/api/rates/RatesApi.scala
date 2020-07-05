package forex.api
package rates

import cats.effect.Sync
import cats.syntax.flatMap._
import forex.programs.RatesProgram
import forex.programs.rates.errors.{ Error => RatesProgramError }
import forex.programs.rates.{ Protocol => RatesProgramProtocol }
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.{ HttpRoutes, Response }

class RatesApi[F[_]: Sync](rates: RatesProgram[F]) extends Http4sDsl[F] {

  import Converters._
  import Protocol._
  import QueryParams._

  private[api] val prefixPath = "/rates"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? FromQueryParam(from) +& ToQueryParam(to) =>
      rates.get(RatesProgramProtocol.GetRatesRequest(from, to)).flatMap {
        case Right(rate) =>
          Ok(rate.asGetApiResponse)

        case Left(error) =>
          errorResponse(error)
      }
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

  private def errorResponse(error: RatesProgramError): F[Response[F]] =
    error match {
      case e @ RatesProgramError.RateNotAvailable(currencies) =>
        NotFound(RateNotFound(currencies.from, currencies.to, e.getMessage))

      case _ =>
        InternalServerError()
    }
}
