package org.sigurdthor.graphql

import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import akka.stream.{ActorMaterializer, Materializer}
import caliban.GraphQL._
import caliban.schema.{GenericSchema, Schema}
import caliban.{CalibanError, GraphQL, Http4sAdapter, RootResolver}
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.sigurdthor.bookshelf.grpc._
import org.sigurdthor.graphql.config.AppConfig
import org.sigurdthor.graphql.model.{AddBookArgs, Mutations, Queries, RecommendationsArgs}
import pureconfig.ConfigSource
import zio._
import zio.console.putStrLn
import zio.interop.catz._
import zio.interop.catz.implicits._

import scala.concurrent.ExecutionContextExecutor

object GqlSchema extends GenericSchema[Any] {

  implicit def seqSchema[A](implicit ev: Schema[Any, A]): Schema[Any, Seq[A]] = listSchema[A].contramap(_.toList)

  implicit val recommendationSchema = gen[Recommendation]
  implicit val addBookSchema = gen[AddBookResponse]
  implicit val recommendationResponseSchema = gen[RecommendationResponse]
  implicit val addBookArgsSchema = gen[AddBookArgs]
  implicit val recommendationArgsSchema = gen[RecommendationsArgs]
}

object GraphqlGateway extends CatsApp {

  import GqlSchema._

  private implicit val actorSystem: ActorSystem = ActorSystem("graphql-grpc-client")
  private implicit val dispatcher: ExecutionContextExecutor = actorSystem.dispatcher
  private implicit val materializer: Materializer = ActorMaterializer()

  private lazy val bookSettings = GrpcClientSettings.connectToServiceAt("localhost", 8443)
  lazy val bookService: BookServiceClient = BookServiceClient(bookSettings)

  private lazy val recommendationSettings = GrpcClientSettings.connectToServiceAt("localhost", 8445)
  lazy val recommendationService: RecommendationServiceClient = RecommendationServiceClient(recommendationSettings)

  override def run(args: List[String]) = (for {
    cfg <- ZIO.fromEither(ConfigSource.default.load[AppConfig])
    interpreter = graphQL(
      RootResolver(
        Queries(
          args => ZIO.fromFuture { implicit ctx =>
            recommendationService.getRecommendations(RecommendationRequest(args.isbn))
          }
        ),
        Mutations(args => ZIO.fromFuture { implicit ctx =>
          bookService.addBook(AddBookRequest(args.isbn, args.title, args.authors, args.description))
        }
        )
      ))
    _ <- runHttp(cfg, interpreter)
  } yield 0).catchAll(err => putStrLn(err.toString).as(1))


  private def runHttp(cfg: AppConfig, interpreter: GraphQL[Any, Queries, Mutations, Unit, CalibanError]) =
    BlazeServerBuilder[Task]
      .bindHttp(cfg.http.port, cfg.http.host)
      .withHttpApp(
        Router(
          "/api/graphql" -> Http4sAdapter.makeRestService(interpreter)
        ).orNotFound
      )
      .resource
      .toManaged
      .useForever
}
