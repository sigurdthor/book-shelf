package org.sigurdthor.book

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import org.sigurdthor.book.api.BookService
import org.sigurdthor.book.domain.{BookEntity, BookSerializerRegistry}
import org.sigurdthor.book.impl.{BookServiceGrpcImpl, BookServiceRestImpl}
import play.api.libs.ws.ahc.AhcWSComponents

class BookLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new BookApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new BookApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[BookService])
}

abstract class BookApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[BookService](wire[BookServiceRestImpl])
    .additionalRouter(wire[BookServiceGrpcImpl])

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry: JsonSerializerRegistry = BookSerializerRegistry

  // Register the book-shelf persistent entity
  persistentEntityRegistry.register(wire[BookEntity])
}
