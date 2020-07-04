package forex.config

import cats.effect.Sync
import pureconfig.{CamelCase, ConfigFieldMapping, ConfigSource}
import pureconfig.generic.ProductHint
import pureconfig.generic.auto._

object Config {

  implicit def productHint[T]: ProductHint[T] =
    ProductHint(
      fieldMapping = ConfigFieldMapping(CamelCase, CamelCase),
      useDefaultArgs = true,
      allowUnknownKeys = false
    )


  /**
    * @param path the property path inside the default configuration
    */
  def load[F[_]: Sync](path: String): F[ApplicationConfig] =
    Sync[F].delay(ConfigSource.default.at(path).loadOrThrow[ApplicationConfig])

}
