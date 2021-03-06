package forex.programs.rates

import cats.Functor
import cats.data.EitherT
import forex.domain._
import forex.programs.RatesProgram
import forex.programs.rates.errors._
import forex.services.RatesService

class Program[F[_]: Functor](ratesService: RatesService[F]) extends Algebra[F] {

  override def get(request: Protocol.GetRatesRequest): F[Either[Error, Rate]] =
    EitherT(ratesService.get(Rate.Currencies(request.from, request.to))).leftMap(toProgramError).value
}

object Program {

  def apply[F[_]: Functor](ratesService: RatesService[F]): RatesProgram[F] =
    new Program[F](ratesService)
}
