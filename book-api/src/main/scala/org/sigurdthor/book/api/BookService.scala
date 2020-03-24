package org.sigurdthor.book.api

import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service}
import org.sigurdthor.book.api.domain.events.BookEventApi


/**
  * The book-shelf service interface.
  * <p>
  * This describes everything that Lagom needs to know about how to serve and
  * consume the BookshelfService.
  */
trait BookService extends Service {

  def bookEvents: Topic[BookEventApi]

  override final def descriptor: Descriptor = {
    import Service._
    // @formatter:off
    named("book-service")
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

