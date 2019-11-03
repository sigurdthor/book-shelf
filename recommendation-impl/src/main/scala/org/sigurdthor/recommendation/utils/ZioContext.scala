package org.sigurdthor.recommendation.utils

import org.sigurdthor.recommendation.repository.{Repositories, RepositoriesLive}
import zio.blocking._
import zio.internal.PlatformLive
import zio.{Runtime, ZIO}

import scala.concurrent.Future

object ZioContext {

  type Environment = Repositories with Blocking

  val env = new RepositoriesLive with Blocking.Live

  val runtime = Runtime(env, PlatformLive.Default)

  def zioCtx[T](block: => ZIO[Environment, Throwable, T]): Future[T] = {
    runtime.unsafeRunToFuture(block)
  }

}
