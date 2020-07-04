package forex.config

import scala.concurrent.duration.FiniteDuration

case class ApiConfig(host: String,
                     port: Int,
                     timeout: FiniteDuration)
