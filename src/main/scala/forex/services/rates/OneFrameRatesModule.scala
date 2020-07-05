package forex.services.rates

import cats.effect._
import cats.syntax.flatMap._
import cats.syntax.functor._
import forex.config.RatesConfig
import forex.domain.Rate
import forex.services.rates.errors.Error.RateNotAvailable
import org.http4s.client.Client

private[rates] object OneFrameRatesModule {

  def create[F[_]: Concurrent : Timer](config: RatesConfig,
                                       httpClient: Client[F]): F[OneFrameRatesModule[F]] =
    for {
      cache <- RatesCacheModule.empty[F]
      oneFrameApi = new OneFrameApiModule[F](config.oneFrameApi, httpClient)
      cacheUpdates = new RatesCacheUpdatesModule[F](config.cache, cache, oneFrameApi)
      _ <- Concurrent[F].start(cacheUpdates.runSynchronousUpdates)
      module = new OneFrameRatesModule(cache)
    } yield module
}

private[rates] class OneFrameRatesModule[F[_]: Sync](cache: RatesCacheModule[F]) extends Algebra[F] {

  override def get(currencies: Rate.Currencies): F[Either[errors.Error, Rate]] =
    cache.get(currencies).map {
      case Some(r) => Right(r)
      case None => Left(RateNotAvailable(currencies))
    }
}
