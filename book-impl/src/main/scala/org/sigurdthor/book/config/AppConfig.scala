package org.sigurdthor.book.config

import pureconfig.ConfigConvert
import pureconfig.generic.semiauto.deriveConvert

final case class AppConfig(grpc: Grpc)

final case class Grpc(host: String, port: Int)

object Grpc {
  implicit val convert: ConfigConvert[Grpc] = deriveConvert
}

object AppConfig {
  implicit val convert: ConfigConvert[AppConfig] = deriveConvert
}