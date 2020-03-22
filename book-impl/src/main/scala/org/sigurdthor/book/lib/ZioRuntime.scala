package org.sigurdthor.book.lib

import io.grpc.ServerBuilder
import org.sigurdthor.book.impl.BookServiceGrpc
import org.sigurdthor.bookshelf.grpc.bookservice.ZioBookservice.BookService
import scalapb.zio_grpc.Server
import zio.clock.Clock
import zio.console.{Console, putStr, putStrLn}
import zio.duration._
import zio.internal.Platform
import zio.{Runtime, ZIO}

trait ZioRuntime {

  def bookService: BookServiceGrpc

  def serverWait: ZIO[Console with Clock, Throwable, Unit] =
    for {
      _ <- putStrLn("GRPC server is running. Press Ctrl-C to stop.")
      _ <- (putStr(".") *> ZIO.sleep(1.second)).forever
    } yield ()

  def serverLive(port: Int) =
    Clock.live >>> bookService.live >>> Server.live[BookService](ServerBuilder.forPort(port))

  def runServer = {
    val runtime = Runtime(Clock, Platform.default)
    runtime.unsafeRunAsync_(serverWait.provideLayer(serverLive(8900) ++ Console.live ++ Clock.live))
  }

}
