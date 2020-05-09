package org.sigurdthor.book.domain

object errors {

  trait ServiceError extends Exception

  case class FieldIsEmpty(field: Field) extends ServiceError

  case object RecordAlreadyExists extends ServiceError

  case object RecordNotFound extends ServiceError

  case class LagomError(message: String) extends ServiceError

  sealed trait Field {
    def name: String
  }

  case object isbn extends Field {
    override def name: String = "isbn"
  }

}
