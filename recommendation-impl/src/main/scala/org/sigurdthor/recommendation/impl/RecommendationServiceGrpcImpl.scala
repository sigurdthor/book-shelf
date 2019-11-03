package org.sigurdthor.recommendation.impl

import akka.actor.ActorSystem
import akka.stream.Materializer
import org.sigurdthor.bookshelf.grpc.{AbstractRecommendationServiceRouter, RecommendationRequest, RecommendationResponse}
import org.sigurdthor.recommendation.repository.Repositories._
import org.sigurdthor.recommendation.utils.ZioContext._

import scala.concurrent.{ExecutionContext, Future}

class RecommendationServiceGrpcImpl(mat: Materializer, system: ActorSystem)(implicit ec: ExecutionContext)
  extends AbstractRecommendationServiceRouter(mat, system) {

  override def getRecommendations(req: RecommendationRequest): Future[RecommendationResponse] = zioCtx {
    retrieveRecommendations(req.isbn).map(RecommendationResponse(_))
  }

}
