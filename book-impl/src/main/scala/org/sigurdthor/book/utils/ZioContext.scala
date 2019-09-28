package org.sigurdthor.book.utils

import org.sigurdthor.book.domain.model.ServiceError
import zio.{DefaultRuntime, IO}

import scala.concurrent.Future

trait ZioContext {

  lazy val runtime = new DefaultRuntime {}

  def zioCtx[T](block: => IO[ServiceError, T]): Future[T] = {
    runtime.unsafeRunToFuture(block)
  }

}
