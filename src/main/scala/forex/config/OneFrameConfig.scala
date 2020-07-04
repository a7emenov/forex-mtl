package forex.config

import java.net.URL

import scala.concurrent.duration.FiniteDuration

case class OneFrameConfig(url: URL,
                          accessToken: String,
                          queryInterval: FiniteDuration)
