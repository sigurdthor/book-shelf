import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import akka.stream.{ActorMaterializer, Materializer}
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import com.typesafe.config.{Config, ConfigFactory}
import graphql.SchemaDefinition
import org.http4s.server.blaze.BlazeServerBuilder
import org.sigurdthor.bookshelf.grpc.{BookServiceClient, RecommendationServiceClient}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object GraphqlGateway extends IOApp with Routes {
  self =>

  lazy val config: Config = ConfigFactory.load

  private implicit val actorSystem: ActorSystem = ActorSystem("graphql-grpc-client")
  private implicit val dispatcher: ExecutionContextExecutor = actorSystem.dispatcher
  private implicit val materializer: Materializer = ActorMaterializer()

  private lazy val bookSettings = GrpcClientSettings.connectToServiceAt("localhost", 8443)
  //GrpcClientSettings.fromConfig("org.sigurdthor.book.BookService")

  lazy val bookService: BookServiceClient = BookServiceClient(bookSettings)

  private lazy val recommendationSettings = GrpcClientSettings.connectToServiceAt("localhost", 8445)
  //GrpcClientSettings.fromConfig("org.sigurdthor.book.BookService")

  lazy val recommendationService: RecommendationServiceClient = RecommendationServiceClient(recommendationSettings)

  implicit val schemaDefinition: SchemaDefinition = new SchemaDefinition {
    override val bookService: BookServiceClient = self.bookService

    override val recommendationService = self.recommendationService

    override implicit val executor: ExecutionContext = self.actorSystem.dispatcher
  }

  override def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(config.getInt("http.port"), config.getString("http.host"))
      .withHttpApp(graphqlServer)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}
