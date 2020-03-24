package org.sigurdthor.book.domain

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import org.sigurdthor.book.api.domain.model.{Author, Book, Description, Title}
import play.api.libs.json.{Format, Json}

import org.sigurdthor.book.lib.JsonFormats._

object commands {

  sealed trait BookCommand[R] extends ReplyType[R]

  case class AddBook(title: Title, authors: Seq[Author], description: Description) extends BookCommand[Done]

  case object GetBook extends BookCommand[Book] {
    implicit val format: Format[GetBook.type] = singletonFormat(GetBook)
  }

  object AddBook {
    implicit val format: Format[AddBook] = Json.format
  }

}
