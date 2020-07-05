package forex

import cats.effect._
import cats.syntax.functor._
import cats.syntax.flatMap._
import forex.config._

import scala.concurrent.ExecutionContext

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    new Application[IO].run.as(ExitCode.Success)

}

class Application[F[_]: ConcurrentEffect: Timer] {

  private val httpClientEc = ExecutionContext.global

  def run: F[Unit] =
    for {
      config <- Config.load[F]("app")
      ratesServiceResource = new RatesModule[F](config.rates, httpClientEc).serviceResource
      _ <- ratesServiceResource.use { ratesProgram =>
        new ApiModule[F](config.api, ratesProgram).startServer.compile.drain
      }
    } yield ()

}
