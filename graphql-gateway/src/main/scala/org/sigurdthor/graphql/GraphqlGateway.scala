package org.sigurdthor.graphql

import caliban.GraphQL._
import caliban.schema.{GenericSchema, Schema}
import caliban.{GraphQLInterpreter, Http4sAdapter, RootResolver}
import com.google.protobuf.ByteString
import io.grpc.ManagedChannelBuilder
import izumi.logstage.api.IzLogger
import izumi.logstage.api.Log.Level.Trace
import izumi.logstage.sink.ConsoleSink
import logstage.{LogBIO, LogstageZIO}
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.sigurdthor.bookshelf.grpc.bookservice.ZioBookservice.BookServiceClient
import org.sigurdthor.bookshelf.grpc.bookservice._
import org.sigurdthor.graphql.config.AppConfig
import org.sigurdthor.graphql.model.{AddBookArgs, Mutations, Queries}
import pureconfig.ConfigSource
import scalapb.zio_grpc.ZManagedChannel
import zio._
import zio.console.putStrLn
import zio.interop.catz._

object GqlSchema extends GenericSchema[BookServiceClient] {

  implicit def seqSchema[A](implicit ev: Schema[BookServiceClient, A]): Schema[BookServiceClient, Seq[A]] = listSchema[A].contramap(_.toList)

  implicit val byteStringSchema: Schema[BookServiceClient, ByteString] = Schema.stringSchema.contramap(_.toStringUtf8)

  implicit val addBookResonseSchema: GqlSchema.Typeclass[AddBookResponse] = gen[AddBookResponse]
  implicit val addBookArgsSchema: GqlSchema.Typeclass[AddBookArgs] = gen[AddBookArgs]
}

object GraphqlGateway extends CatsApp {

  type AppTask[A] = RIO[ZEnv, A]

  import GqlSchema._

  lazy val textSink: ConsoleSink = ConsoleSink.text(colored = true)
  lazy val izLogger: IzLogger = IzLogger(Trace, List(textSink))
  lazy val log: LogBIO[IO] = LogstageZIO.withFiberId(izLogger)

  def clientLayer: Layer[Throwable, BookServiceClient] = BookServiceClient.live(
    ZManagedChannel(
      ManagedChannelBuilder.forAddress("0.0.0.0", 8900).usePlaintext()
    )
  )

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    (for {
      cfg <- ZIO.fromEither(ConfigSource.default.load[AppConfig])
      schema = graphQL(
        RootResolver(
          Queries(),
          Mutations(args =>
            log.debug(s"Perorming request with args $args") *>
              BookServiceClient.addBook(AddBookRequest(args.isbn, args.title, args.authors, args.description))
                .mapError(_.asException()))
        ))
      interpreter <- schema.interpreter.map(_.provideCustomLayer(clientLayer))
      _ <- runHttp(cfg, interpreter)
    } yield 0).catchAll(err => putStrLn(err.toString).as(1))

  private def runHttp(cfg: AppConfig, interpreter: GraphQLInterpreter[ZEnv, Throwable]): ZIO[ZEnv, Throwable, Unit] =
    for {
      _ <- BlazeServerBuilder[AppTask]
        .bindHttp(cfg.http.port, cfg.http.host)
        .withHttpApp(
          Router(
            "/api/graphql" -> Http4sAdapter.makeHttpService(interpreter)
          ).orNotFound
        )
        .resource
        .toManaged
        .useForever
    } yield ()
}
