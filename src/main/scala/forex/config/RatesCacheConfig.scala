package forex.config

import scala.concurrent.duration.FiniteDuration

case class RatesCacheConfig(refreshInterval: FiniteDuration)
