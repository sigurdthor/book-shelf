import java.util.concurrent.TimeUnit

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
import scala.concurrent.duration.Duration

object GraphqlGateway extends IOApp with Routes {
  self =>

  lazy val config: Config = ConfigFactory.load

  private implicit val actorSystem: ActorSystem = ActorSystem("graphql-grpc-client")
  private implicit val dispatcher: ExecutionContextExecutor = actorSystem.dispatcher
  private implicit val materializer: Materializer = ActorMaterializer()

  private lazy val settings = GrpcClientSettings
    .connectToServiceAt("127.0.0.1", 8443)
    .withDeadline(Duration.create(600, TimeUnit.SECONDS)) // response timeout
    .withConnectionAttempts(5) // use a small reconnectionAttempts value to cause a client reload in case of failure

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
