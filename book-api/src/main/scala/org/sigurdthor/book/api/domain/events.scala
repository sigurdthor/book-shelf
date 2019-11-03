package org.sigurdthor.book.api.domain

import julienrf.json.derived
import org.sigurdthor.book.api.domain.model.{Author, Description, ISBN, Title}
import play.api.libs.json.{Format, Json, __}

object events {

  sealed trait BookEventApi {
     def isbn: ISBN
  }

  object BookEventApi {
    implicit val format: Format[BookEventApi] = derived.flat.oformat((__ \ "type").format[String])
  }

  case class BookAddedApi(isbn: ISBN, title: Title, authors: Seq[Author], description: Description) extends BookEventApi

  object BookAddedApi {
    implicit val format: Format[BookAddedApi] = Json.format
  }

}
