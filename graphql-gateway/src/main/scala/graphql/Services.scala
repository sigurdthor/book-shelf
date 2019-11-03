package graphql

import org.sigurdthor.bookshelf.grpc.{BookServiceClient, RecommendationServiceClient}

trait Services {

  def bookService: BookServiceClient

  def recommendationService: RecommendationServiceClient

}
