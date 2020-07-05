package forex.services.rates.cache

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.syntax.functor._
import forex.domain.Rate

object RatesCacheModule {

  def empty[F[_]: Sync]: F[RatesCacheAlgebra[F]] =
    Ref.of(Map.empty[Rate.Currencies, Rate]).map(new RatesCacheModule(_))
}

private[rates] class RatesCacheModule[F[_]: Sync](ref: Ref[F, Map[Rate.Currencies, Rate]])
    extends RatesCacheAlgebra[F] {

  def get(currencies: Rate.Currencies): F[Option[Rate]] =
    ref.get.map(_.get(currencies))

  def update(rates: Map[Rate.Currencies, Rate]): F[Unit] =
    ref.update(_ ++ rates)
}
