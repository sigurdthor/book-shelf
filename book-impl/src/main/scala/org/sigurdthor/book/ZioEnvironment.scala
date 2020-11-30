package org.sigurdthor.book

import com.typesafe.scalalogging.StrictLogging
import io.grpc.ServerBuilder
import org.sigurdthor.book.config.AppConfig
import org.sigurdthor.book.impl.BookServiceGrpc
import org.sigurdthor.bookshelf.grpc.bookservice.ZioBookservice.BookService
import pureconfig.ConfigSource
import scalapb.zio_grpc.ServerLayer
import zio.clock.Clock
import zio.console.{Console, putStr, putStrLn}
import zio.duration._
import zio.{Runtime, ZIO}

class ZioEnvironment(bookService: BookServiceGrpc) extends StrictLogging {

  def service: ZIO[Console with Clock, Throwable, Unit] =
    for {
      _ <- putStrLn("Book service is running")
      _ <- (putStr(".") *> ZIO.sleep(1.second)).forever
    } yield ()

  def run() = {
    ConfigSource.default.load[AppConfig] match {
      case Right(cfg) =>
        val serverLayer = bookService.live >>> ServerLayer.access[BookService](ServerBuilder.forPort(cfg.grpc.port))
        val app = service.provideSomeLayer[zio.ZEnv](serverLayer ++ Console.live ++ Clock.live)
        Runtime.default.unsafeRunAsync_(app)

      case Left(error) => logger.error("Config reading failure", error)
    }

  }
}
