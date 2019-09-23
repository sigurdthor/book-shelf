package org.sigurdthor.book.domain

import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag}
import org.sigurdthor.book.api.domain.model.{Author, Description, ISBN, Title}
import play.api.libs.json.{Format, Json}

object events {

  sealed trait BookEvent extends AggregateEvent[BookEvent] {
    def aggregateTag: AggregateEventTag[BookEvent] = BookshelfEvent.Tag
  }

  object BookshelfEvent {
    val Tag: AggregateEventTag[BookEvent] = AggregateEventTag[BookEvent]
  }

  case class BookAdded(isbn: ISBN, title: Title, authors: Seq[Author], description: Description) extends BookEvent

  object BookAdded {
    implicit val format: Format[BookAdded] = Json.format
  }

}
