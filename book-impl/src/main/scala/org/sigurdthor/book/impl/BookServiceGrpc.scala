package org.sigurdthor.book.impl

import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityRef}
import akka.util.Timeout
import io.grpc.Status
import org.sigurdthor.book.api.domain.model._
import org.sigurdthor.book.domain.BookEntity
import org.sigurdthor.book.domain.commands._
import org.sigurdthor.book.domain.errors.{FieldIsEmpty, RecordAlreadyExists, RecordNotFound}
import org.sigurdthor.book.domain.validation.Validator
import org.sigurdthor.book.lib.Transformations._
import org.sigurdthor.bookshelf.grpc.bookservice.ZioBookservice.BookService
import org.sigurdthor.bookshelf.grpc.bookservice._
import org.sigurdthor.lib.Logger
import zio._

import scala.concurrent.duration._


class BookServiceGrpc(clusterSharding: ClusterSharding)
                     (implicit validator: Validator[Book]) extends Logger {

  type BookServiceM = Has[ZioBookservice.BookService]

  implicit val timeout = Timeout(5.seconds)

  def live: Layer[Nothing, BookServiceM] = ZLayer.fromFunction {
    _ =>
      new BookService {

        def addBook(req: AddBookRequest): IO[Status, AddBookResponse] = {
          val flow = for {
            book <- validator.validate(req.toBook)
            r <- IO.fromFuture { _ =>
              entityRef(book.isbn.value)
                .ask(reply => AddBook(book.title, book.authors, book.description, reply))
            } <* log.debug(s"Book with ISBN ${book.isbn.value} is added")
          } yield r match {
            case BookAlreadyExists => IO.fail(RecordAlreadyExists)
            case BookAddedReply(addedAt) => IO.succeed(AddBookResponse(addedAt.toString))
          }

          flow.flatten.mapError {
            case FieldIsEmpty(_) => Status.FAILED_PRECONDITION
            case RecordAlreadyExists => Status.ALREADY_EXISTS
            case _ => Status.INTERNAL
          }
        }

        def getBook(req: GetBookRequest): IO[Status, BookResponse] =
          IO.fromFuture { implicit ec =>
            entityRef(req.isbn).ask(reply => GetBook(reply))
              .map {
                case BookNotFound => IO.fail(RecordNotFound)
                case reply@(_: BookReply) => IO.succeed(reply.toResponse)
              }
          }.flatten.mapError {
            case RecordNotFound => Status.NOT_FOUND
            case _ => Status.INTERNAL
          }
      }
  }

  private def entityRef(id: String): EntityRef[Command] =
    clusterSharding.entityRefFor(BookEntity.typeKey, id)
}
