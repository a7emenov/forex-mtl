package forex.services.rates

import cats.effect.{Concurrent, Timer}
import cats.syntax.functor._
import forex.config.OneFrameConfig
import forex.services.rates
import org.http4s.client.Client

object Modules {

  def oneFrame[F[_]: Concurrent : Timer](config: OneFrameConfig,
                                         httpClient: Client[F]): F[rates.Algebra[F]] =
    OneFrameModule.create(config, httpClient).widen
}
