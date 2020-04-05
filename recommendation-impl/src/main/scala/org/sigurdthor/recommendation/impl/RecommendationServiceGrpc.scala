package org.sigurdthor.recommendation.impl

import io.grpc.Status
import org.sigurdthor.bookshelf.grpc.recommendationservice.{RecommendationRequest, RecommendationResponse, ZioRecommendationservice}
import org.sigurdthor.recommendation.repository.RecommendationRepository._
import zio.{Has, ZLayer}

class RecommendationServiceGrpc {

  type RecommendationServiceM = Has[ZioRecommendationservice.RecommendationService]

  def live: ZLayer[RecommendationRepo, Nothing, RecommendationServiceM] = ZLayer.fromService {
    repository =>
      (request: RecommendationRequest) =>
        repository.findRecommendations(request.query)
          .bimap(
            _ => Status.NOT_FOUND,
            RecommendationResponse(_)
          )
  }
}
