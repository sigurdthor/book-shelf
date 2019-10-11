import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import akka.stream.{ActorMaterializer, Materializer}
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import com.typesafe.config.{Config, ConfigFactory}
import graphql.SchemaDefinition
import org.http4s.server.blaze.BlazeServerBuilder
import org.sigurdthor.bookshelf.grpc.BookServiceClient

import scala.concurrent.ExecutionContextExecutor

object GraphqlGateway extends IOApp with Routes {
  self =>

  lazy val config: Config = ConfigFactory.load

  private implicit val actorSystem: ActorSystem = ActorSystem("graphql-grpc-client")
  private implicit val dispatcher: ExecutionContextExecutor = actorSystem.dispatcher
  private implicit val materializer: Materializer = ActorMaterializer()

  private lazy val settings = GrpcClientSettings.fromConfig("org.sigurdthor.book.BookService")

  lazy val bookService: BookServiceClient = BookServiceClient(settings)

  implicit val schemaDefinition: SchemaDefinition = new SchemaDefinition {
    override def bookService: BookServiceClient = self.bookService
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
