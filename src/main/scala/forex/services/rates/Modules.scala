package forex.services.rates

import cats.effect.Sync
import forex.config.OneFrameConfig
import forex.services.rates
import org.http4s.client.Client

object Modules {

  def oneFrame[F[_]: Sync](config: OneFrameConfig,
                           client: Client[F]): rates.Algebra[F] =
    new OneFrameModule[F](config, client)
}
