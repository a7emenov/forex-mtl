package util

import java.time.{ OffsetDateTime, ZoneOffset }

import forex.domain.{ Currency, Price, Rate, Timestamp }
import org.scalacheck.Gen

object Generators {

  val currency: Gen[Currency] =
    Gen.oneOf(Currency.values)

  val price: Gen[Price] =
    Gen
      .frequency(
        (99, Gen.posNum[Double]),
        (0, Gen.const[Double](0))
      )
      .map(v => Price(BigDecimal(v)))

  val timestamp: Gen[Timestamp] =
    Gen.calendar
      .map(c => Timestamp(OffsetDateTime.ofInstant(c.toInstant, ZoneOffset.UTC)))

  val rateCurrencies: Gen[Rate.Currencies] =
    for {
      from <- currency
      to <- currency
    } yield Rate.Currencies(from, to)

  val rate: Gen[Rate] =
    for {
      currencies <- rateCurrencies
      price <- price
      timestamp <- timestamp
    } yield Rate(currencies, price, timestamp)

  val rateMap: Gen[Map[Rate.Currencies, Rate]] =
    Gen.nonEmptyMap(rate.map(r => r.currencies -> r))
}
