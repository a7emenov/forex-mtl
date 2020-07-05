package forex.programs.rates

import cats.syntax.show._
import forex.domain.Rate
import forex.services.rates.errors.{ Error => RatesServiceError }

object errors {

  sealed trait Error extends Exception
  object Error {
    final case class RateNotAvailable(currencies: Rate.Currencies) extends Error {
      override def getMessage: String =
        s"Rate is not available for currencies: ${currencies.from.show} to ${currencies.to.show}"
    }
  }

  def toProgramError(error: RatesServiceError): Error = error match {
    case RatesServiceError.RateNotAvailable(currencies) =>
      Error.RateNotAvailable(currencies)
  }
}
