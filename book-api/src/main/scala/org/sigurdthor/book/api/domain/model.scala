package org.sigurdthor.book.api.domain

import play.api.libs.json.{Format, Json}

object model {

  case class ISBN(value: String) extends AnyVal

  case class Title(value: String) extends AnyVal

  case class Author(value: String) extends AnyVal

  case class Description(value: String) extends AnyVal

  case class Book(isbn: ISBN, title: Title, authors: Seq[Author], description: Description)

  object Book {
    implicit val format: Format[Book] = Json.format
  }

  object ISBN {
    implicit val format: Format[ISBN] = Json.format
  }

  object Title {
    implicit val format: Format[Title] = Json.format
  }

  object Author {
    implicit val format: Format[Author] = Json.format
  }

  object Description {
    implicit val format: Format[Description] = Json.format
  }
}
