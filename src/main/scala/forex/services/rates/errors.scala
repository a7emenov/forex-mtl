package forex.services.rates

import forex.domain.Rate

object errors {

  sealed trait Error
  object Error {
    final case class RateNotAvailable(currencies: Rate.Currencies) extends Error
  }
}
