package org.sigurdthor.book.api

import java.time.OffsetDateTime

import cats.data.NonEmptyList
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import org.sigurdthor.book.api.domain.model.{Author, Book, Description, ISBN, Title}
import play.api.libs.json.{Format, Json}


/**
  * The book-shelf service interface.
  * <p>
  * This describes everything that Lagom needs to know about how to serve and
  * consume the BookshelfService.
  */
trait BookService extends Service {

  def addBook: ServiceCall[AddBookRequest, AddBookResponse]


  override final def descriptor: Descriptor = {
    import Service._
    // @formatter:off
    named("book-shelf")
      .withCalls(
        restCall(Method.POST, "/api/book", addBook _),
      )
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

