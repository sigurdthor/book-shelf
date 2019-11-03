package org.sigurdthor.book.api

import java.time.OffsetDateTime

import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import org.sigurdthor.book.api.domain.events.BookEventApi
import org.sigurdthor.book.api.domain.model.Book
import play.api.libs.json.{Format, Json}


/**
  * The book-shelf service interface.
  * <p>
  * This describes everything that Lagom needs to know about how to serve and
  * consume the BookshelfService.
  */
trait BookService extends Service {

  def addBook: ServiceCall[AddBookRequest, AddBookResponse]

  def bookEvents: Topic[BookEventApi]

  override final def descriptor: Descriptor = {
    import Service._
    // @formatter:off
    named("book-service")
      .withCalls(
        restCall(Method.POST, "/api/book", addBook _),
      )
      .withTopics(
        topic("book-events", bookEvents)
          .addProperty(
            KafkaProperties.partitionKeyStrategy,
            PartitionKeyStrategy[BookEventApi](_.isbn.value)
          ))
      .withAutoAcl(true)
    // @formatter:on
  }
}


case class AddBookRequest(book: Book)

case class AddBookResponse(addedAt: OffsetDateTime)

object AddBookRequest {
  implicit val format: Format[AddBookRequest] = Json.format
}

object AddBookResponse {
  implicit val format: Format[AddBookResponse] = Json.format
}

