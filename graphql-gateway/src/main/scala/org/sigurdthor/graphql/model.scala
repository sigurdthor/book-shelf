package org.sigurdthor.graphql

import org.sigurdthor.bookshelf.grpc.{AddBookResponse, RecommendationResponse}
import zio.Task

object model {

  case class Queries(recommendations: RecommendationsArgs => Task[RecommendationResponse])

  case class Mutations(addBook: AddBookArgs => Task[AddBookResponse])

  case class RecommendationsArgs(isbn: String)

  case class AddBookArgs(isbn: String, title: String, authors: List[String], description: String)

}
