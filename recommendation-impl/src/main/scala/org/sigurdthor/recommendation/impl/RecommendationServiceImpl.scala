package org.sigurdthor.recommendation.impl

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.ServiceCall
import org.sigurdthor.recommendation.api.RecommendationService
import org.sigurdthor.recommendation.api.domain.model

/**
  * Implementation of the BookshelfService.
  */
class RecommendationServiceImpl extends RecommendationService {
  override def searchForRecommendations(isbn: String): ServiceCall[NotUsed, List[model.Recommendation]] = ???
}
