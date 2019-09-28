package graphql

import sangria.schema.{Field, ObjectType, Schema, fields}

trait SchemaDefinition extends Mutations with Types with Services {

  val queryType = ObjectType(
    "Query",
    () ⇒
      fields[SecureContext, Unit](
        Field("book", BookType, resolve = ctx => Book("isbn", "Great book")
        )))

  val schema = Schema(queryType, Some(MutationType), None)
}
