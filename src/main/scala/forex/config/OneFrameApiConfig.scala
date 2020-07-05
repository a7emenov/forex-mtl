package forex.config

import org.http4s.Uri

case class OneFrameApiConfig(url: Uri,
                             accessToken: Secret)
