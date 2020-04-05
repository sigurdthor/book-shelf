package org.sigurdthor.recommendation.api.domain

import play.api.libs.json.{Format, Json}

object model {

  case class Recommendation(isbn: String, authors: List[String], title: String)

  object Recommendation {
    implicit val format: Format[Recommendation] = Json.format
  }
}
