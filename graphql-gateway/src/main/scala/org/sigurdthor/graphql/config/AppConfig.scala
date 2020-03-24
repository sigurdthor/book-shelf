package org.sigurdthor.graphql.config

import pureconfig.ConfigConvert
import pureconfig.generic.semiauto.deriveConvert


final case class AppConfig(http: Http)

final case class Http(host: String, port: Int)

object Http {
  implicit val convert: ConfigConvert[Http] = deriveConvert
}

object AppConfig {
  implicit val convert: ConfigConvert[AppConfig] = deriveConvert
}