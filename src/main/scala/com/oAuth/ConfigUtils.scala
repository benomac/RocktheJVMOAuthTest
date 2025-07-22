package com.oAuth

import ciris.circe.circeConfigDecoder
import ciris.{ConfigDecoder, Secret}
import io.circe.Decoder

object ConfigUtils {
  case class Credential(key: String, secret: Secret[String])
  case class AppConfig(credentials: Map[String, Credential]) derives Decoder

  given appDecoder: Decoder[Credential] = Decoder.instance { h =>
    for {
      key <- h.get[String]("key")
      secret <- h.get[String]("secret")
    } yield Credential(key, Secret(secret))
  }

  given ConfigDecoder[String, AppConfig] = circeConfigDecoder[AppConfig]("AppConfig")


}
