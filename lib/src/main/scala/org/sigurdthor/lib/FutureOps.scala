package org.sigurdthor.lib

import com.typesafe.scalalogging.StrictLogging
import zio.{IO, Runtime, Task}

import scala.concurrent.{ExecutionContext, Future}

object FutureOps extends StrictLogging {

  implicit class FutureToZio[T](f: Future[T])(implicit ec: ExecutionContext) {

    def toTask: Task[T] = IO.fromFuture { implicit ec =>
      f.recoverWith {
        case e: Throwable =>
          logger.error("Future failed", e)
          Future.failed(e)
      }
    }
  }

  implicit class ZioFromFuture[T](task: Task[T]) {

    def toScalaFuture: Future[T] = {
      Runtime.default.unsafeRunToFuture(task)
    }
  }

}
