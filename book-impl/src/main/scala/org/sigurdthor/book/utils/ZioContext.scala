package org.sigurdthor.book.utils

import org.sigurdthor.book.domain.model.ServiceError
import zio.{DefaultRuntime, IO}

import scala.concurrent.Future

trait ZioContext {

  def zioCtx[T](block: => IO[ServiceError, T]): Future[T] = {
    val runtime = new DefaultRuntime {}
    runtime.unsafeRunToFuture(block)
  }

}
