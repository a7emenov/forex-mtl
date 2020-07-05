package forex

import cats.effect.{ConcurrentEffect, Resource, Timer}
import cats.syntax.functor._
import forex.config.OneFrameConfig
import forex.programs.rates.{Algebra, Program}
import forex.services.rates
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext

class RatesModule[F[_]: ConcurrentEffect : Timer](config: OneFrameConfig,
                                                  httpClientEc: ExecutionContext) {

  private val client: Resource[F, Client[F]] = BlazeClientBuilder[F](httpClientEc).resource

  val serviceResource: Resource[F, Algebra[F]] =
    client.evalMap(cl => rates.Modules.oneFrame(config, cl).map(Program(_)))
}
