package org.sigurdthor.book.impl

import java.time.OffsetDateTime

import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import io.grpc.Status
import org.sigurdthor.book.api.domain.model._
import org.sigurdthor.book.domain.BookEntity
import org.sigurdthor.book.domain.commands.{AddBook, GetBook}
import org.sigurdthor.book.domain.model.FieldIsEmpty
import org.sigurdthor.book.domain.validation.Validator
import org.sigurdthor.book.lib.Transformations._
import org.sigurdthor.bookshelf.grpc.bookservice.ZioBookservice.BookService
import org.sigurdthor.bookshelf.grpc.bookservice._
import org.sigurdthor.lib.Logger
import zio._
import org.sigurdthor.book.lib.ErrorLogger._


class BookServiceGrpc(persistentEntityRegistry: PersistentEntityRegistry)
                     (implicit validator: Validator[Book]) extends Logger {

  type BookServiceM = Has[ZioBookservice.BookService]

  def live: Layer[Nothing, BookServiceM] = ZLayer.fromFunction {
    _ =>
      new BookService {

        def addBook(req: AddBookRequest): IO[Status, AddBookResponse] = {
          val flow = for {
            book <- validator.validate(req.toBook)
            _ <- IO.fromFuture { implicit ec =>
              bookEntityRef(book.isbn.value).ask(AddBook(book.title, book.authors, book.description)).logError
            } *> log.debug(s"Book ${req.isbn} has been added")
          } yield AddBookResponse(OffsetDateTime.now().toString)

          flow.mapError {
            case FieldIsEmpty(_) => Status.FAILED_PRECONDITION
            case _ => Status.INTERNAL
          }
        }

        def getBook(req: GetBookRequest): IO[Status, BookResponse] =
          IO.fromFuture { implicit ec =>
            bookEntityRef(req.isbn).ask(GetBook).logError
          }.bimap(
            _ => Status.NOT_FOUND,
            _.toResponse)
      }
  }

  private def bookEntityRef(bookId: String) = persistentEntityRegistry.refFor[BookEntity](bookId)
}
