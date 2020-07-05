package forex.domain

object Rate {
  case class Currencies(from: Currency, to: Currency)

  def apply(from: Currency, to: Currency, price: Price, timestamp: Timestamp): Rate =
    Rate(Currencies(from, to), price, timestamp)
}

case class Rate(currencies: Rate.Currencies, price: Price, timestamp: Timestamp)
