package forex.config

import cats.effect.Sync
import com.typesafe.config.ConfigRenderOptions
import org.http4s.Uri
import pureconfig.error.CannotConvert
import pureconfig.generic.ProductHint
import pureconfig.generic.auto._
import pureconfig._

object Config {

  implicit def productHint[T]: ProductHint[T] =
    ProductHint(
      fieldMapping = ConfigFieldMapping(CamelCase, CamelCase),
      useDefaultArgs = true,
      allowUnknownKeys = false
    )

  implicit val uriReader: ConfigReader[Uri] =
    ConfigReader[String].emap(s => Uri.fromString(s).left.map(e => CannotConvert(s, "org.http4s.Uri", e.sanitized)))

  implicit val uriWriter: ConfigWriter[Uri] =
    ConfigWriter[String].contramap(_.renderString)

  implicit val secretReader: ConfigReader[Secret] =
    ConfigReader[String].map(Secret)

  implicit val secretWriter: ConfigWriter[Secret] =
    ConfigWriter[String].contramap(_ => "****")

  /**
    * @param path the property path inside the default configuration
    */
  def load[F[_]: Sync](path: String): F[ApplicationConfig] =
    Sync[F].delay(ConfigSource.default.at(path).loadOrThrow[ApplicationConfig])

  def write(config: ApplicationConfig): String =
    ConfigWriter[ApplicationConfig]
      .to(config)
      .render(
        ConfigRenderOptions
          .defaults()
          .setFormatted(true)
          .setJson(true)
          .setComments(false)
          .setOriginComments(false)
      )
}
