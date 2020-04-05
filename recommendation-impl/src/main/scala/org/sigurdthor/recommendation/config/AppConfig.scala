package org.sigurdthor.recommendation.config

import pureconfig.ConfigConvert
import pureconfig.generic.semiauto.deriveConvert

final case class AppConfig(grpc: Grpc, elasticsearch: Elasticsearch)

final case class Grpc(host: String, port: Int)
final case class Elasticsearch(host: String, port: String)

object Grpc {
  implicit val convert: ConfigConvert[Grpc] = deriveConvert
}

object Elasticsearch {
  implicit val convert: ConfigConvert[Elasticsearch] = deriveConvert
}

object AppConfig {
  implicit val convert: ConfigConvert[AppConfig] = deriveConvert
}