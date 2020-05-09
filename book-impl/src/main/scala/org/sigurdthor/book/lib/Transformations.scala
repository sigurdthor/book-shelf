package org.sigurdthor.book.lib

import org.sigurdthor.book.api.domain.model.{Author, Book, Description, ISBN, Title}
import org.sigurdthor.book.domain.{BookEntity}
import io.scalaland.chimney.dsl._
import org.sigurdthor.book.domain.commands.BookReply
import org.sigurdthor.bookshelf.grpc.bookservice.{AddBookRequest, BookResponse}

object Transformations {

  implicit class EntityTransformer(entity: BookEntity) {

    def toReply(isbn: ISBN): BookReply =
      entity
        .into[BookReply]
        .withFieldComputed(_.isbn, _ => isbn)
        .withFieldComputed(_.title, _ => entity.title.get)
        .withFieldComputed(_.authors, _ => entity.authors)
        .withFieldComputed(_.description, _ => entity.description.get)
        .transform
  }

  implicit class BookTransformer(book: BookReply) {

    def toResponse: BookResponse =
      book
        .into[BookResponse]
        .withFieldComputed(_.isbn, _ => book.isbn.value)
        .withFieldComputed(_.title, _ => book.title.value)
        .withFieldComputed(_.authors, _ => book.authors.map(_.value))
        .withFieldComputed(_.description, _ => book.description.value)
        .transform

  }

  implicit class BookRequestTransformer(req: AddBookRequest) {

    def toBook: Book =
      req
        .into[Book]
        .withFieldComputed(_.isbn, _ => ISBN(req.isbn))
        .withFieldComputed(_.title, _ => Title(req.title))
        .withFieldComputed(_.authors, _ => req.authors.map(Author(_)))
        .withFieldComputed(_.description, _ => Description(req.description))
        .transform
  }

}
