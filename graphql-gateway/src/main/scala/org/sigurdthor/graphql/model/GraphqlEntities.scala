package org.sigurdthor.graphql.model

import org.sigurdthor.bookshelf.grpc.bookservice.ZioBookservice.BookServiceClient
import org.sigurdthor.bookshelf.grpc.bookservice.{AddBookResponse, BookResponse}
import org.sigurdthor.bookshelf.grpc.recommendationservice.RecommendationResponse
import org.sigurdthor.bookshelf.grpc.recommendationservice.ZioRecommendationservice.RecommendationServiceClient
import zio._

object GraphqlEntities {

  case class Queries(book: GetBookArgs => RIO[BookServiceClient, BookResponse],
                     recommendations: RecommendationsArgs => RIO[RecommendationServiceClient, RecommendationResponse])

  case class Mutations(addBook: AddBookArgs => RIO[BookServiceClient, AddBookResponse])

  case class AddBookArgs(isbn: String, title: String, authors: List[String], description: String)

  case class GetBookArgs(isbn: String)

  case class RecommendationsArgs(query: String)
}
