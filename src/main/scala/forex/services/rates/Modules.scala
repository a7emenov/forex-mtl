package forex.services.rates

import cats.effect.{Concurrent, Timer}
import cats.syntax.functor._
import forex.config.RatesConfig
import forex.services.RatesService
import org.http4s.client.Client

object Modules {

  def oneFrame[F[_]: Concurrent : Timer](config: RatesConfig,
                                         httpClient: Client[F]): F[RatesService[F]] =
    OneFrameRatesModule.create(config, httpClient).widen
}
