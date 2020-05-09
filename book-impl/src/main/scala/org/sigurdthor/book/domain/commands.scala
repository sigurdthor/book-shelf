package org.sigurdthor.book.domain

import java.time.OffsetDateTime

import akka.actor.typed.ActorRef
import julienrf.json.derived
import org.sigurdthor.book.api.domain.model.{Author, Description, ISBN, Title}
import play.api.libs.json.{Format, Json, __}
import org.sigurdthor.book.lib.JsonFormats._

object commands {

  trait CommandSerializable

  sealed trait Command extends CommandSerializable

  final case class AddBook(title: Title, authors: Seq[Author], description: Description, replyTo: ActorRef[AddBookReply]) extends Command

  final case class GetBook(replyTo: ActorRef[GetBookReply]) extends Command

  sealed trait AddBookReply

  final case class BookAddedReply(addedAt: OffsetDateTime) extends AddBookReply

  object BookAddedReply {
    implicit val format: Format[BookAddedReply] = Json.format
  }

  final case object BookAlreadyExists extends AddBookReply {
    implicit val format: Format[BookAlreadyExists.type] = singletonFormat(BookAlreadyExists)
  }

  object AddBookReply {
    implicit val format: Format[AddBookReply] = derived.flat.oformat((__ \ "type").format[String])
  }

  sealed trait GetBookReply

  case class BookReply(isbn: ISBN, title: Title, authors: Seq[Author], description: Description) extends GetBookReply

  object BookReply {
    implicit val format: Format[BookReply] = Json.format
  }

  case object BookNotFound extends GetBookReply {
    implicit val format: Format[BookNotFound.type] = singletonFormat(BookNotFound)
  }

  object GetBookReply {
    implicit val format: Format[GetBookReply] = derived.flat.oformat((__ \ "type").format[String])
  }
}
