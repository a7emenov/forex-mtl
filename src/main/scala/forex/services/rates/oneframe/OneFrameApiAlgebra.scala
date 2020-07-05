package forex.services.rates.oneframe

import cats.data.NonEmptyList
import forex.domain.Rate

private[rates] trait OneFrameApiAlgebra[F[_]] {

  def getRates(currencies: NonEmptyList[Rate.Currencies]): F[Map[Rate.Currencies, Rate]]
}
