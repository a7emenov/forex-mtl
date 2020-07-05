package forex.api.rates

import forex.domain._

object Converters {
  import Protocol._

  private[rates] implicit class GetApiResponseOps(val rate: Rate) extends AnyVal {
    def asGetApiResponse: GetApiResponse =
      GetApiResponse(
        from = rate.currencies.from,
        to = rate.currencies.to,
        price = rate.price,
        timestamp = rate.timestamp
      )
  }
}
