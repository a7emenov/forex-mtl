package forex.services.rates

import forex.domain.{Currency, Rate}
import errors._

trait Algebra[F[_]] {
  def get(from: Currency, to: Currency): F[Error Either Rate]
}
