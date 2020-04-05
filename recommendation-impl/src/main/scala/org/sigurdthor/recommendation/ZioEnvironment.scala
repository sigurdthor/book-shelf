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
import scalapb.zio_grpc.Server
import zio.clock.Clock
import zio.console.{Console, putStr, putStrLn}
import zio.duration._
import zio.{Runtime, ZIO}

import scala.concurrent.ExecutionContext


class ZioEnvironment(recommendationService: RecommendationServiceGrpc, bookService: BookService)
                    (implicit val ec: ExecutionContext) extends ElasticConnection with StrictLogging {

  def serverWait: ZIO[Console with Clock, Throwable, Unit] =
    for {
      _ <- putStrLn("Recommendation is running")
      _ <- (putStr(".") *> ZIO.sleep(1.second)).forever
    } yield ()

  def run() = {
    ConfigSource.default.load[AppConfig] match {
      case Right(cfg) =>
        val serviceLayer = elasticClientLayer(cfg.elasticsearch) >>> RecommendationRepository.live >>> recommendationService.live
        val consumerLayer = elasticClientLayer(cfg.elasticsearch) >>> RecommendationRepository.live >>> new EventsConsumer(bookService).live
        val grpcServerLayer = serviceLayer >>> Server.live[RecommendationService](ServerBuilder.forPort(cfg.grpc.port))
        val appLayer = grpcServerLayer ++ consumerLayer ++ Console.live ++ Clock.live

        val app = serverWait.as(ZIO.accessM[EventsConsumer#BookEventConsumer](_.get.subscribeOnEvents))
          .provideSomeLayer[zio.ZEnv](appLayer)

        Runtime.default.unsafeRunAsync_(app)

      case Left(error) => logger.error("Config reading failure", error)
    }
  }
}
