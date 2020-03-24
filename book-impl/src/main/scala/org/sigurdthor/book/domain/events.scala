package org.sigurdthor.book.domain

import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventShards, AggregateEventTag}
import julienrf.json.derived
import org.sigurdthor.book.api.domain.model.{Author, Description, ISBN, Title}
import play.api.libs.json.{Format, Json, __}

object events {

  sealed trait BookEvent extends AggregateEvent[BookEvent] {
    def aggregateTag: AggregateEventShards[BookEvent] = BookEvent.Tag
  }

  object BookEvent {
    val NumShards = 5
    val Tag: AggregateEventShards[BookEvent] = AggregateEventTag.sharded[BookEvent](NumShards)

    implicit val format: Format[BookEvent] = derived.flat.oformat((__ \ "type").format[String])
  }

  case class BookAdded(isbn: String, title: Title, authors: Seq[Author], description: Description) extends BookEvent

  object BookAdded {
    implicit val format: Format[BookAdded] = Json.format
  }

}
