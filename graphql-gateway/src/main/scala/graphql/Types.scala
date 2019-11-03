package graphql

import org.sigurdthor.bookshelf.grpc.Recommendation
import sangria.schema.{Field, ListType, ObjectType, StringType, fields}

case class Book(isbn: String, title: String)

trait Types {

  lazy val RecommendationType: ObjectType[SecureContext, Recommendation] =
    ObjectType(
      "Recommendation",
      "Recommendation info.",
      fields[SecureContext, Recommendation](
        Field("isbn", StringType, resolve = _.value.isbn),
        Field("title", StringType, resolve = _.value.title),
        Field("authors", ListType(StringType), resolve = _.value.authors)
      )
    )
}
