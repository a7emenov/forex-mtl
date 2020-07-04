package forex.api.rates

import forex.domain.Currency
import org.http4s.QueryParamDecoder
import org.http4s.dsl.impl.QueryParamDecoderMatcher

object QueryParams {

  private[api] implicit val currencyQueryParam: QueryParamDecoder[Currency] =
    QueryParamDecoder[String].map(Currency.fromString)

  object FromQueryParam extends QueryParamDecoderMatcher[Currency]("from")
  object ToQueryParam extends QueryParamDecoderMatcher[Currency]("to")

}
