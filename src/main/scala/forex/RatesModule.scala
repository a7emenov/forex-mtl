package forex

import cats.effect.{ConcurrentEffect, Resource}
import forex.config.OneFrameConfig
import forex.programs.rates.{Algebra, Program}
import forex.services.rates
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext

class RatesModule[F[_]: ConcurrentEffect](config: OneFrameConfig,
                                          httpClientEc: ExecutionContext) {

  private val client: Resource[F, Client[F]] = BlazeClientBuilder[F](httpClientEc).resource

  val serviceResource: Resource[F, Algebra[F]] =
    client.map(cl => Program(rates.Modules.oneFrame(config, cl)))
}
