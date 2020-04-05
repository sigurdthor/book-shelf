package org.sigurdthor.lib

import izumi.logstage.api.IzLogger
import izumi.logstage.api.Log.Level.Trace
import izumi.logstage.sink.ConsoleSink
import logstage.{LogBIO, LogstageZIO}
import zio.IO

trait Logger {

  lazy val textSink: ConsoleSink = ConsoleSink.text(colored = true)
  lazy val izLogger: IzLogger = IzLogger(Trace, List(textSink))
  lazy val log: LogBIO[IO] = LogstageZIO.withFiberId(izLogger)
}
