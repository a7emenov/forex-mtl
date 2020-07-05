package forex.services.rates

import cats.data.NonEmptyList
import cats.effect.{Concurrent, Sync, Timer}
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.syntax.functor._
import forex.config.RatesCacheConfig
import forex.domain.{Currency, Rate}
import wvlet.log.LogSupport

private[rates] class RatesCacheUpdatesModule[F[_]: Concurrent : Timer](config: RatesCacheConfig,
                                                                       cache: RatesCacheModule[F],
                                                                       oneFrameApi: OneFrameApiModule[F]) extends LogSupport {
  private val ratesCombinations = {
    val combinations = for {
      from <- Currency.values
      to <- Currency.values if from != to
    } yield Rate.Currencies(from, to)
    NonEmptyList.fromListUnsafe(combinations.toList)
  }

  private val updateCache: F[Unit] = {
    val update = for {
      rates <- oneFrameApi.getRates(ratesCombinations)
      _ <- Sync[F].delay(logger.info("Cache updated"))
      result <- cache.update(rates)
    } yield result

    update.handleErrorWith(e => Sync[F].delay(logger.error(s"Cache update failed: ${e.getMessage}")))
  }

  val runSynchronousUpdates: F[Unit] = {
    def updates(): F[Unit] = for {
      _ <- updateCache
      _ <- Timer[F].sleep(config.refreshInterval)
      result <- updates()
    } yield result

    (Sync[F].delay(logger.info("New cache updates process started")) >> updates())
      .handleErrorWith { e =>
        Sync[F].delay(logger.error(s"Cache updates process failed: ${e.getMessage}")) >>
        runSynchronousUpdates
      }
  }
}
