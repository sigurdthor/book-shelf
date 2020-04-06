package org.sigurdthor.recommendation.consumers

import _root_.zio.{Has, Task, ZLayer}
import akka.Done
import akka.stream.scaladsl.Flow
import org.sigurdthor.book.api._
import org.sigurdthor.book.api.domain.events.{BookAddedApi, BookEventApi}
import org.sigurdthor.lib.FutureOps._
import org.sigurdthor.lib.Logger
import org.sigurdthor.recommendation.repository.RecommendationRepository.RecommendationRepo

import scala.concurrent.ExecutionContext

class EventsConsumer(bookService: BookService)(implicit val ec: ExecutionContext) extends Logger {

  type BookEventConsumer = Has[BookEventConsumer.Service]

  object BookEventConsumer {

    trait Service {
      def subscribeOnEvents: Task[Done]
    }

  }

  val live: ZLayer[RecommendationRepo, Nothing, BookEventConsumer] = ZLayer.fromService { repository =>
    new BookEventConsumer.Service {
      override def subscribeOnEvents: Task[Done] = {
        bookService.bookEvents.subscribe
          .withGroupId("recommendation-service")
          .atLeastOnce(
            Flow[BookEventApi]
              .mapAsync(1) {
                case event@BookAddedApi(isbn, title, authors, _) =>
                  (log.debug(s"Consuming event $event") *>
                    repository.addBook(isbn, title, authors).as(Done))
                    .toScalaFuture
              }
          ).toTask
      }
    }
  }

}
