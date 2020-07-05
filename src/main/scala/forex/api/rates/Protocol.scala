package forex.api
package rates

import forex.domain._
import io.circe._
import io.circe.generic.semiauto._

object Protocol {

  final case class GetApiRequest(
      from: Currency,
      to: Currency
  )

  final case class GetApiResponse(
      from: Currency,
      to: Currency,
      price: Price,
      timestamp: Timestamp
  )

  final case class RateNotFound(from: Currency, to: Currency, message: String)

  implicit val currencyEncoder: Encoder[Currency] =
    Encoder.instance[Currency] { Currency.showInstance.show _ andThen Json.fromString }

  implicit val rateCurrenciesEncoder: Encoder[Rate.Currencies] =
    deriveEncoder[Rate.Currencies]

  implicit val rateEncoder: Encoder[Rate] =
    deriveEncoder[Rate]

  implicit val responseEncoder: Encoder[GetApiResponse] =
    deriveEncoder[GetApiResponse]

  implicit val notFoundEncoder: Encoder[RateNotFound] =
    deriveEncoder[RateNotFound]
}
