package org.sigurdthor.recommendation

import com.lightbend.lagom.scaladsl.akka.discovery.AkkaDiscoveryComponents
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaClientComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import org.sigurdthor.book.api.BookService
import org.sigurdthor.recommendation.api.RecommendationService
import org.sigurdthor.recommendation.impl.{RecommendationServiceGrpc, RecommendationServiceImpl}
import play.api.libs.ws.ahc.AhcWSComponents

import scala.concurrent.ExecutionContext

class Loader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new RecommendationApplication(context) with AkkaDiscoveryComponents with LagomKafkaClientComponents

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new RecommendationApplication(context) with LagomDevModeComponents with LagomKafkaClientComponents

  override def describeService = Some(readDescriptor[RecommendationService])
}

abstract class RecommendationApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  lazy val bookService: BookService = serviceClient.implement[BookService]

  implicit val ec: ExecutionContext = actorSystem.dispatcher

  val recommendationService: RecommendationServiceGrpc = wire[RecommendationServiceGrpc]
  val zioEnvironment: ZioEnvironment = wire[ZioEnvironment]

  zioEnvironment.run()

  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[RecommendationService](wire[RecommendationServiceImpl])
}
