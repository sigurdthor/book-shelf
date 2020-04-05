package org.sigurdthor.recommendation.api

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import org.sigurdthor.recommendation.api.domain.model.Recommendation


/**
  * The book-shelf service interface.
  * <p>
  * This describes everything that Lagom needs to know about how to serve and
  * consume the BookshelfService.
  */
trait RecommendationService extends Service {

  def searchForRecommendations(query: String): ServiceCall[NotUsed, List[Recommendation]]

  override final def descriptor: Descriptor = {
    import Service._
    // @formatter:off
    named("recommendation-service")
      .withCalls(
        restCall(Method.POST, "/api/book/recommendations", searchForRecommendations _)
      )
      .withAutoAcl(true)
    // @formatter:on
  }
}




