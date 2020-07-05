package forex.services.rates

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.syntax.functor._
import forex.domain.Rate

object RatesCacheModule {

  def empty[F[_]: Sync]: F[RatesCacheModule[F]] =
    Ref.of(Map.empty[Rate.Currencies, Rate]).map(new RatesCacheModule(_))
}

private[rates] class RatesCacheModule[F[_]: Sync] private (ref: Ref[F, Map[Rate.Currencies, Rate]]) {

  def get(currencies: Rate.Currencies): F[Option[Rate]] =
    ref.get.map(_.get(currencies))

  def update(rates: Map[Rate.Currencies, Rate]): F[Unit] =
    ref.update(_ ++ rates)
}
