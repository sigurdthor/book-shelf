package org.sigurdthor.book.domain

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import org.sigurdthor.book.api.domain.model.{Author, Description, Title}
import play.api.libs.json.{Format, Json}

object commands {

  sealed trait BookCommand[R] extends ReplyType[R]

  case class AddBook(title: Title, authors: Seq[Author], description: Description) extends BookCommand[Done]

  object AddBook {
    implicit val format: Format[AddBook] = Json.format
  }
}
