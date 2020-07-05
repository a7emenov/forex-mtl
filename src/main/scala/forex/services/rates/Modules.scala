package forex.services.rates

import cats.effect.{Concurrent, Timer}
import cats.syntax.functor._
import forex.config.RatesConfig
import forex.services.rates
import org.http4s.client.Client

object Modules {

  def oneFrame[F[_]: Concurrent : Timer](config: RatesConfig,
                                         httpClient: Client[F]): F[rates.Algebra[F]] =
    OneFrameModule.create(config, httpClient).widen
}
