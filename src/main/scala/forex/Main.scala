package forex

import cats.effect._
import cats.syntax.flatMap._
import cats.syntax.functor._
import forex.config._
import wvlet.log.{ LogFormatter, LogSupport, Logger }

import scala.concurrent.ExecutionContext

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    new Application[IO].run.as(ExitCode.Success)

}

class Application[F[_]: ConcurrentEffect: Timer] extends LogSupport {

  private val httpClientEc = ExecutionContext.global

  private def initLogging: F[Unit] =
    Sync[F].delay {
      Logger.init
      Logger.setDefaultFormatter(LogFormatter.PlainSourceCodeLogFormatter)
      Logger.scanLogLevels
    }

  def run: F[Unit] =
    for {
      _ <- initLogging
      config <- Config.load[F]("app")
      _ <- Sync[F].delay(logger.info(Config.write(config)))
      ratesServiceResource = new RatesModule[F](config.rates, httpClientEc).serviceResource
      _ <- ratesServiceResource.use { ratesProgram =>
            new ApiModule[F](config.api, ratesProgram).startServer.compile.drain
          }
    } yield ()

}
