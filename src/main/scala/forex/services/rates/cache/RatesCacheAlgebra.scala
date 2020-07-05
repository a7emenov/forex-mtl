package forex.services.rates.cache

import forex.domain.Rate

private[rates] trait RatesCacheAlgebra[F[_]] {

  def get(currencies: Rate.Currencies): F[Option[Rate]]

  def update(rates: Map[Rate.Currencies, Rate]): F[Unit]
}
