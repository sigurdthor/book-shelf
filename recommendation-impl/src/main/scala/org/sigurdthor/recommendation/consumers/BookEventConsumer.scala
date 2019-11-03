package org.sigurdthor.recommendation.consumers

import akka.Done
import akka.stream.scaladsl.Flow
import org.sigurdthor.book.api._
import org.sigurdthor.book.api.domain.events.{BookAddedApi, BookEventApi}
import org.sigurdthor.recommendation.repository.Repositories._
import org.sigurdthor.recommendation.utils.ZioContext._

import scala.concurrent.ExecutionContext

class BookEventConsumer(bookService: BookService)(implicit val ec: ExecutionContext) {

  bookService.bookEvents.subscribe
    .withGroupId("recommendation-service")
    .atLeastOnce(
      Flow[BookEventApi]
        .mapAsync(1) {
          case event@BookAddedApi(isbn, title, authors, _) =>
            zioCtx {
              addBook(event.isbn, title, authors).map(_ => Done)
            }
        })
}
