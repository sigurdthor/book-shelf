package org.sigurdthor.book.impl

import akka.persistence.query.Offset
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}
import org.sigurdthor.book.api.BookService
import org.sigurdthor.book.api.domain.events.{BookAddedApi, BookEventApi}
import org.sigurdthor.book.api.domain.model.ISBN
import org.sigurdthor.book.domain.events.{BookAdded, BookEvent}

/**
  * Implementation of the BookshelfService.
  */
class BookServiceImpl(persistentEntityRegistry: PersistentEntityRegistry) extends BookService {

  override def bookEvents: Topic[BookEventApi] = TopicProducer.taggedStreamWithOffset(BookEvent.Tag.allTags.toList) { (tag, offset) =>
    persistentEntityRegistry
      .eventStream(tag, offset)
      .filter {
        _.event match {
          case _@(_: BookAdded) => true
          case _ => false
        }
      }
      .map(convertEvent)
  }

  private def convertEvent(eventStreamElement: EventStreamElement[BookEvent]): (BookEventApi, Offset) = {
    eventStreamElement match {
      case EventStreamElement(_, BookAdded(isbn, title, authors, description), offset) =>
        val message = BookAddedApi(ISBN(isbn), title, authors, description)
        (message, offset)
    }
  }
}
