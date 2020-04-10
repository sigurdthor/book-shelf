package org.sigurdthor.graphql.service

import io.grpc.ManagedChannelBuilder
import org.sigurdthor.bookshelf.grpc.bookservice.ZioBookservice.BookServiceClient
import org.sigurdthor.bookshelf.grpc.recommendationservice.ZioRecommendationservice.RecommendationServiceClient
import scalapb.zio_grpc.ZManagedChannel

object GrpcLayer {

  lazy val bookClientLayer = BookServiceClient.live(
    ZManagedChannel(
      ManagedChannelBuilder.forAddress("0.0.0.0", 8909).usePlaintext()
    )
  )

  lazy val recommendationClientLayer = RecommendationServiceClient.live(
    ZManagedChannel(
      ManagedChannelBuilder.forAddress("0.0.0.0", 8910).usePlaintext()
    )
  )
}
