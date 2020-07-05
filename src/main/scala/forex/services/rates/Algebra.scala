package forex.services.rates

import forex.domain.Rate
import forex.services.rates.errors._

trait Algebra[F[_]] {
  def get(currencies: Rate.Currencies): F[Error Either Rate]
}
