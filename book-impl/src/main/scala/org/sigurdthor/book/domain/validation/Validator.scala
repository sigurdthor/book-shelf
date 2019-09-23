package org.sigurdthor.book.domain.validation

import org.sigurdthor.book.api.domain.model.Book
import org.sigurdthor.book.domain.model.{FieldIsEmpty, ServiceError, isbn}
import zio.IO

trait Validator[T] {
  def validate(entity: T): IO[ServiceError, T]
}


object Validator {
  implicit val bookValidator: Validator[Book] = (entity: Book) => if (entity.isbn.value.isEmpty) IO.fail(FieldIsEmpty(isbn)) else IO.succeed(entity)
}