package org.sigurdthor.book.utils

import org.sigurdthor.book.domain.model.ServiceError
import zio.clock.Clock
import zio.internal.PlatformLive
import zio.{IO, Runtime}

import scala.concurrent.Future

object ZioContext {

  lazy val runtime = Runtime(Clock, PlatformLive.Default)

  def zioCtx[T](block: => IO[ServiceError, T]): Future[T] = {
    runtime.unsafeRunToFuture(block)
  }

}
