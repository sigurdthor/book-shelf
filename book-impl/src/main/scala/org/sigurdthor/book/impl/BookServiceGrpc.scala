package org.sigurdthor.book.impl

import java.time.OffsetDateTime

import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import io.grpc.Status
import izumi.logstage.api.IzLogger
import izumi.logstage.api.Log.Level.Trace
import izumi.logstage.sink.ConsoleSink
import logstage.{LogBIO, LogstageZIO}
import org.sigurdthor.book.api.domain.model._
import org.sigurdthor.book.domain.BookEntity
import org.sigurdthor.book.domain.commands.AddBook
import org.sigurdthor.book.domain.validation.Validator
import org.sigurdthor.bookshelf.grpc.bookservice.ZioBookservice.BookService
import org.sigurdthor.bookshelf.grpc.bookservice.{AddBookRequest, AddBookResponse, ZioBookservice}
import zio.clock.Clock
import zio.{Has, IO, ZLayer}


class BookServiceGrpc(persistentEntityRegistry: PersistentEntityRegistry)(implicit validator: Validator[Book]) {

  type BookServiceM = Has[ZioBookservice.BookService]

  lazy val textSink: ConsoleSink = ConsoleSink.text(colored = true)
  lazy val izLogger: IzLogger = IzLogger(Trace, List(textSink))
  lazy val log: LogBIO[IO] = LogstageZIO.withFiberId(izLogger)

  def live: ZLayer[Clock, Nothing, BookServiceM] = ZLayer.fromService {
    _ =>
      new BookService {
        def addBook(req: AddBookRequest): IO[Status, AddBookResponse] = {
          val flow = for {
            book <- log.debug(s"Validating book ${req.isbn}") *> validator.validate(Book(ISBN(req.isbn), Title(req.title), req.authors.map(Author(_)), Description(req.description)))
            _ <- IO.fromFuture { implicit ec => bookEntityRef(book.isbn).ask(AddBook(book.title, book.authors, book.description)) } *> log.debug(s"Book ${req.isbn} is added")
          } yield AddBookResponse(OffsetDateTime.now().toString)

          flow.mapError(_ => Status.FAILED_PRECONDITION)
        }

        private def bookEntityRef(bookId: ISBN) = persistentEntityRegistry.refFor[BookEntity](bookId.value)
      }
  }
}
