package forex.config

import scala.concurrent.duration.FiniteDuration

case class HttpConfig(host: String,
                      port: Int,
                      timeout: FiniteDuration)
