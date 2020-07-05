package forex.services.rates

import cats.data.NonEmptyList
import cats.effect._
import cats.effect.concurrent.Ref
import cats.syntax.flatMap._
import cats.syntax.functor._
import forex.config.OneFrameConfig
import forex.domain.{Currency, Rate}
import forex.services.rates.errors.Error.RateNotAvailable
import org.http4s.circe.CirceInstances
import org.http4s.client.Client

import scala.concurrent.duration.FiniteDuration

private[rates] object OneFrameModule {

  private val ratesCombinations = {
    val combinations = for {
      from <- Currency.values
      to <- Currency.values if from != to
    } yield Rate.Currencies(from, to)
    NonEmptyList.fromListUnsafe(combinations.toList)
  }

  private def updateCache[F[_]: Sync](cache: Ref[F, Map[Rate.Currencies, Rate]],
                                      oneFrameApi: OneFrameApi[F]): F[Unit] = {
    // todo: logging
    for {
      rates <- oneFrameApi.rates(ratesCombinations)
      result <- rates match {
        case Right(rs) =>
          cache.set(rs)

        case Left(e) =>
          Sync[F].unit
      }
    } yield result
  }

  private def synchronousCacheUpdates[F[_]: Sync : Timer](cache: Ref[F, Map[Rate.Currencies, Rate]],
                                                          oneFrameApi: OneFrameApi[F],
                                                          queryInterval: FiniteDuration): F[Unit] = {
    for {
      _ <- updateCache(cache, oneFrameApi)
      _ <- Timer[F].sleep(queryInterval)
      result <- synchronousCacheUpdates(cache, oneFrameApi, queryInterval)
    } yield result
  }

  def create[F[_]: Concurrent : Timer](config: OneFrameConfig,
                                       httpClient: Client[F]): F[OneFrameModule[F]] =
    for {
      cache <- Ref.of(Map.empty[Rate.Currencies, Rate])
      oneFrameApi = new OneFrameApi[F](config, httpClient)
      _ <- Concurrent[F].start(synchronousCacheUpdates(cache, oneFrameApi, config.queryInterval))
      module = new OneFrameModule(config, cache)
    } yield module
}

private[rates] class OneFrameModule[F[_]: Sync](config: OneFrameConfig,
                                                cache: Ref[F, Map[Rate.Currencies, Rate]]) extends Algebra[F] with CirceInstances {

  override def get(currencies: Rate.Currencies): F[Either[errors.Error, Rate]] =
    for {
      cacheSnapshot <- cache.get
      result = cacheSnapshot.get(currencies)
    } yield result match {
      case Some(r) => Right(r)
      case None => Left(RateNotAvailable(currencies))
    }
}
