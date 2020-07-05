package forex

import cats.effect.{ConcurrentEffect, ExitCode, Timer}
import forex.api.rates.RatesApi
import forex.config.ApiConfig
import forex.programs._
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.{AutoSlash, Timeout}

class ApiModule[F[_]: ConcurrentEffect: Timer](config: ApiConfig,
                                               ratesProgram: RatesProgram[F]) {


  type PartialMiddleware = HttpRoutes[F] => HttpRoutes[F]
  type TotalMiddleware   = HttpApp[F] => HttpApp[F]

  private val routesMiddleware: PartialMiddleware =
    { http => AutoSlash(http) }

  private val appMiddleware: TotalMiddleware =
  { http => Timeout(config.timeout)(http) }

  private val ratesHttpRoutes: HttpRoutes[F] = new RatesApi[F](ratesProgram).routes

  private val http: HttpRoutes[F] = ratesHttpRoutes

  private val httpApp: HttpApp[F] = appMiddleware(routesMiddleware(http).orNotFound)

  val startServer: fs2.Stream[F, ExitCode] = BlazeServerBuilder[F]
    .withoutBanner
    .bindHttp(config.port, config.host)
    .withHttpApp(httpApp)
    .serve
}
