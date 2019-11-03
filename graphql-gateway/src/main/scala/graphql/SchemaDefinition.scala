package graphql

import org.sigurdthor.bookshelf.grpc.RecommendationRequest
import sangria.schema.{Argument, Field, ListType, ObjectType, StringType, Schema, fields}

trait SchemaDefinition extends Mutations with Types with Services with ConcurrencyProvider {

  lazy val isbnArgument = Argument("isbn", StringType)

  val queryType = ObjectType(
    "Query",
    () ⇒
      fields[SecureContext, Unit](
        Field("recommendations",
          ListType(RecommendationType),
          arguments = isbnArgument :: Nil,
          resolve = ctx =>
            recommendationService.getRecommendations(RecommendationRequest(ctx.arg(isbnArgument)))
              .map(_.recommendations)
        )))

  val schema = Schema(queryType, Some(MutationType), None)
}
