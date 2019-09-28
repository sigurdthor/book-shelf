package graphql

import sangria.schema.{Field, ObjectType, StringType, fields}

case class Book(isbn: String, title: String)

trait Types {

  lazy val BookType: ObjectType[SecureContext, Book] =
    ObjectType(
      "Book",
      "Book info.",
      fields[SecureContext, Book](
        Field("isbn", StringType, resolve = _.value.isbn),
        Field("title", StringType, resolve = _.value.title)
      )
    )
}
