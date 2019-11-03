package org.sigurdthor.book.impl

import java.time.OffsetDateTime

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import org.sigurdthor.book.api.domain.model.{Author, Book, Description, ISBN, Title}
import org.sigurdthor.book.domain.BookEntity
import org.sigurdthor.book.domain.commands.AddBook
import org.sigurdthor.book.domain.model.LagomError
import org.sigurdthor.book.domain.validation.Validator
import org.sigurdthor.book.utils.ZioContext._
import org.sigurdthor.bookshelf.grpc.{AbstractBookServiceRouter, AddBookRequest, AddBookResponse}
import zio.IO

import scala.concurrent.{ExecutionContext, Future}

class BookServiceGrpcImpl(persistentEntityRegistry: PersistentEntityRegistry, mat: Materializer, system: ActorSystem)(implicit ec: ExecutionContext, validator: Validator[Book])
  extends AbstractBookServiceRouter(mat, system) {

  override def addBook(req: AddBookRequest): Future[AddBookResponse] = zioCtx {
    for {
      book <- validator.validate(Book(ISBN(req.isbn), Title(req.title), req.authors.map(Author(_)), Description(req.description)))
      _ <- IO.fromFuture { implicit ec => bookEntityRef(book.isbn).ask(AddBook(book.title, book.authors, book.description)) }.mapError(f => LagomError(f.getMessage))
    } yield AddBookResponse(OffsetDateTime.now().toString)
  }


  private def bookEntityRef(bookId: ISBN) = persistentEntityRegistry.refFor[BookEntity](bookId.value)
}
