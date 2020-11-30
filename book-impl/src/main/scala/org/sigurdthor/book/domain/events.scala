package org.sigurdthor.book.domain

import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventShards, AggregateEventTag, AggregateEventTagger}
import org.sigurdthor.book.api.domain.model.{Author, Description, ISBN, Title}
import play.api.libs.json.{Format, Json}

object events {

  sealed trait BookEvent extends AggregateEvent[BookEvent] {
    override def aggregateTag: AggregateEventTagger[BookEvent] = BookEvent.Tag
  }

  object BookEvent {
    val Tag: AggregateEventShards[BookEvent] = AggregateEventTag.sharded[BookEvent](numShards = 10)
  }

  case class BookAdded(isbn: ISBN, title: Title, authors: Seq[Author], description: Description) extends BookEvent

  object BookAdded {
    implicit val format: Format[BookAdded] = Json.format
  }

}
