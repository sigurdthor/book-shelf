package org.sigurdthor.book.api

import java.util.UUID

import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
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


case class AddBookRequest(name: String)
case class AddBookResponse(id: UUID, name: String)

object AddBookRequest {
  implicit val format: Format[AddBookRequest] = Json.format
}

object AddBookResponse {
  implicit val format: Format[AddBookResponse] = Json.format
}

