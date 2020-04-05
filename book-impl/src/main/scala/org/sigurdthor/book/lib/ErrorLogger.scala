package org.sigurdthor.book.lib

import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.{ExecutionContext, Future}

object ErrorLogger extends StrictLogging {

  implicit class FutureLogger[T](f: Future[T])(implicit ec: ExecutionContext) {

    def logError =
      f.recoverWith {
        case e: Throwable =>
          logger.error("Command failure", e)
          Future.failed(e)
      }
  }

}
