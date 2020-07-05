package forex.config

import cats.effect.Sync
import org.http4s.Uri
import pureconfig.error.CannotConvert
import pureconfig.{CamelCase, ConfigFieldMapping, ConfigReader, ConfigSource}
import pureconfig.generic.ProductHint
import pureconfig.generic.auto._

object Config {

  implicit def productHint[T]: ProductHint[T] =
    ProductHint(
      fieldMapping = ConfigFieldMapping(CamelCase, CamelCase),
      useDefaultArgs = true,
      allowUnknownKeys = false
    )

  implicit val uriReader: ConfigReader[Uri] =
    ConfigReader[String].emap(s => Uri.fromString(s).left.map(e => CannotConvert(s, "org.http4s.Uri", e.sanitized)))


  /**
    * @param path the property path inside the default configuration
    */
  def load[F[_]: Sync](path: String): F[ApplicationConfig] =
    Sync[F].delay(ConfigSource.default.at(path).loadOrThrow[ApplicationConfig])

}
