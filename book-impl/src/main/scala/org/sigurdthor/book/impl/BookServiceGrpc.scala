package org.sigurdthor.book.impl

import java.time.OffsetDateTime

import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.typesafe.scalalogging.StrictLogging
import io.grpc.Status
import izumi.logstage.api.IzLogger
import izumi.logstage.api.Log.Level.Trace
import izumi.logstage.sink.ConsoleSink
import logstage.{LogBIO, LogstageZIO}
import org.sigurdthor.book.api.domain.model._
import org.sigurdthor.book.domain.BookEntity
import org.sigurdthor.book.domain.commands.{AddBook, GetBook}
import org.sigurdthor.book.domain.validation.Validator
import org.sigurdthor.bookshelf.grpc.bookservice.ZioBookservice.BookService
import org.sigurdthor.bookshelf.grpc.bookservice.{AddBookRequest, AddBookResponse, BookResponse, GetBookRequest, ZioBookservice}
import zio.clock.Clock
import zio.{Has, IO, ZLayer}
import org.sigurdthor.book.lib.Transformations._

import scala.concurrent.Future


class BookServiceGrpc(persistentEntityRegistry: PersistentEntityRegistry)(implicit validator: Validator[Book]) extends StrictLogging {

  type BookServiceM = Has[ZioBookservice.BookService]

  lazy val textSink: ConsoleSink = ConsoleSink.text(colored = true)
  lazy val izLogger: IzLogger = IzLogger(Trace, List(textSink))
  lazy val log: LogBIO[IO] = LogstageZIO.withFiberId(izLogger)

  def live: ZLayer[Clock, Nothing, BookServiceM] = ZLayer.fromService {
    _ =>
      new BookService {
        def addBook(req: AddBookRequest): IO[Status, AddBookResponse] = {
          val flow = for {
            book <- validator.validate(req.toBook)
            _ <- IO.fromFuture { implicit ec =>
              bookEntityRef(book.isbn.value).ask(AddBook(book.title, book.authors, book.description)).recoverWith {
                case e: Throwable =>
                  logger.error("addBook command failure", e)
                  Future.failed(e)
              }
            } *> log.debug(s"Book ${req.isbn} has been added")
          } yield AddBookResponse(OffsetDateTime.now().toString)

          flow.mapError(_ => Status.FAILED_PRECONDITION)
        }

        def getBook(req: GetBookRequest): IO[Status, BookResponse] =
          log.debug(s"getBook Request $req") *> IO.fromFuture { implicit ec =>
            bookEntityRef(req.isbn).ask(GetBook).recoverWith {
              case e: Throwable =>
                logger.error("getBook command failure", e)
                Future.failed(e)
            }
          }.bimap(
            _ => Status.NOT_FOUND,
            _.toResponse)
      }
  }

  private def bookEntityRef(bookId: String) = persistentEntityRegistry.refFor[BookEntity](bookId)
}
