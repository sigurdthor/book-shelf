package org.sigurdthor.recommendation

import com.typesafe.scalalogging.StrictLogging
import io.grpc.ServerBuilder
import org.sigurdthor.book.api.BookService
import org.sigurdthor.bookshelf.grpc.recommendationservice.ZioRecommendationservice.RecommendationService
import org.sigurdthor.recommendation.config.AppConfig
import org.sigurdthor.recommendation.consumers.EventsConsumer
import org.sigurdthor.recommendation.impl.RecommendationServiceGrpc
import org.sigurdthor.recommendation.repository.{ElasticConnection, RecommendationRepository}
import pureconfig.ConfigSource
import scalapb.zio_grpc.ServerLayer
import zio.clock.Clock
import zio.console.{Console, putStrLn}
import zio.{Runtime, ZIO}

import scala.concurrent.ExecutionContext


class ZioEnvironment(recommendationService: RecommendationServiceGrpc, bookService: BookService)
                    (implicit val ec: ExecutionContext) extends ElasticConnection with StrictLogging {

  def service: ZIO[Console with EventsConsumer#BookEventConsumer, Throwable, Unit] =
    for {
      _ <- putStrLn("Recommendation service is running")
      _ <- ZIO.accessM[EventsConsumer#BookEventConsumer](_.get.subscribeOnEvents)
    } yield ()

  def run() = {
    ConfigSource.default.load[AppConfig] match {
      case Right(cfg) =>
        val serviceLayer = elasticClientLayer(cfg.elasticsearch) >>> RecommendationRepository.live >>> recommendationService.live
        val consumerLayer = elasticClientLayer(cfg.elasticsearch) >>> RecommendationRepository.live >>> new EventsConsumer(bookService).live
        val grpcServerLayer = serviceLayer >>> ServerLayer.access[RecommendationService](ServerBuilder.forPort(cfg.grpc.port))
        val appLayer = grpcServerLayer ++ consumerLayer ++ Console.live ++ Clock.live

        val app = service.provideSomeLayer[zio.ZEnv](appLayer)

        Runtime.default.unsafeRunAsync_(app)

      case Left(error) => logger.error("Config reading failure", error)
    }
  }
}
