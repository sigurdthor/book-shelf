package org.sigurdthor.recommendation.domain.codecs

import io.circe.{Decoder, HCursor}
import org.apache.commons.lang3.StringUtils
import org.sigurdthor.bookshelf.grpc.recommendationservice.Recommendation

object CirceCodecs {

  implicit val decodeRecommendation: Decoder[Recommendation] = (c: HCursor) => for {
    isbn <- c.downField("isbn").as[String]
    title <- c.downField("title").as[String]
    authors <- c.downField("authors").as[String]
  } yield new Recommendation(isbn, title, authors.split(StringUtils.SPACE).toSeq)
}
