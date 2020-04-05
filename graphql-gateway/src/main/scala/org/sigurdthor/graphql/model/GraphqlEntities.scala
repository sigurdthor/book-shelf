package org.sigurdthor.graphql.model

import org.sigurdthor.bookshelf.grpc.bookservice.{AddBookResponse, BookResponse}
import org.sigurdthor.bookshelf.grpc.recommendationservice.RecommendationResponse
import org.sigurdthor.graphql.GraphqlGateway.Composite
import zio._

object GraphqlEntities {

  case class Queries(book: GetBookArgs => RIO[Composite, BookResponse],
                     recommendations: RecommendationsArgs => RIO[Composite, RecommendationResponse])

  case class Mutations(addBook: AddBookArgs => RIO[Composite, AddBookResponse])

  case class AddBookArgs(isbn: String, title: String, authors: List[String], description: String)

  case class GetBookArgs(isbn: String)

  case class RecommendationsArgs(query: String)
}
