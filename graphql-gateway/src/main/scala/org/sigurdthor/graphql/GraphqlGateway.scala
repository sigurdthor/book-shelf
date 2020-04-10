package org.sigurdthor.graphql

import caliban.GraphQL.graphQL
import caliban.schema.{GenericSchema, Schema}
import caliban.{GraphQLInterpreter, Http4sAdapter, RootResolver}
import com.google.protobuf.ByteString
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import org.sigurdthor.bookshelf.grpc.bookservice.ZioBookservice.BookServiceClient
import org.sigurdthor.bookshelf.grpc.bookservice.{AddBookResponse, BookResponse, GetBookRequest}
import org.sigurdthor.bookshelf.grpc.recommendationservice.ZioRecommendationservice.RecommendationServiceClient
import org.sigurdthor.bookshelf.grpc.recommendationservice.{Recommendation, RecommendationRequest, RecommendationResponse}
import org.sigurdthor.graphql.GraphqlGateway.Composite
import org.sigurdthor.graphql.config.AppConfig
import org.sigurdthor.graphql.model.GraphqlEntities._
import org.sigurdthor.graphql.model.Transformations._
import org.sigurdthor.graphql.service.GrpcLayer._
import pureconfig.ConfigSource
import zio._
import zio.console.putStrLn
import zio.interop.catz._
import org.sigurdthor.graphql.service.ErrorHandler._

object GraphqlSchema extends GenericSchema[Composite] {

  implicit val byteStringSchema: Schema[Composite, ByteString] = Schema.stringSchema.contramap(_.toStringUtf8)

  implicit val addBookResponseSchema = gen[AddBookResponse]
  implicit val bookResponseSchema = gen[BookResponse]
  implicit val addBookArgsSchema = gen[AddBookArgs]
  implicit val getBookArgsSchema = gen[GetBookArgs]

  implicit val recommendationResponseSchema = gen[RecommendationResponse]
  implicit val recommendationSchema = gen[Recommendation]
  implicit val recommendationsArgsSchema = gen[RecommendationsArgs]

  val api = graphQL(
    RootResolver(
      Queries(args =>
        BookServiceClient.getBook(GetBookRequest(args.isbn))
          .mapError(_.asException()),
        args => RecommendationServiceClient.searchForRecommendations(RecommendationRequest(args.query))
          .mapError(_.asException())
      ),
      Mutations(args =>
        BookServiceClient.addBook(args.toRequest)
          .mapError(_.asException()))
    ))
}

object GraphqlGateway extends CatsApp {

  type AppTask[A] = RIO[ZEnv, A]
  type Composite = RecommendationServiceClient with BookServiceClient

  import GraphqlSchema._

  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    (for {
      cfg <- ZIO.fromEither(ConfigSource.default.load[AppConfig])
      interpreter <- api.interpreter.map(_.provideCustomLayer(recommendationClientLayer ++ bookClientLayer))
      _ <- runHttp(cfg, withErrorCodeExtensions(interpreter))
    } yield 0).catchAll(err => putStrLn(err.toString).as(1))

  private def runHttp(cfg: AppConfig, interpreter: GraphQLInterpreter[ZEnv, Throwable]): ZIO[ZEnv, Throwable, Unit] =
    for {
      _ <- BlazeServerBuilder[AppTask]
        .bindHttp(cfg.http.port, cfg.http.host)
        .withHttpApp(
          Router(
            "/api/graphql" -> CORS(Http4sAdapter.makeHttpService(interpreter))
          ).orNotFound
        )
        .resource
        .toManaged
        .useForever
    } yield ()
}
