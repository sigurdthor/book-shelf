package org.sigurdthor.book

import akka.cluster.sharding.typed.scaladsl.Entity
import com.lightbend.lagom.scaladsl.akka.discovery.AkkaDiscoveryComponents
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import org.sigurdthor.book.api.BookService
import org.sigurdthor.book.domain.{BookEntity, BookSerializerRegistry}
import org.sigurdthor.book.impl.{BookServiceGrpc, BookServiceImpl}
import play.api.libs.ws.ahc.AhcWSComponents

class BookLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new BookApplication(context) with AkkaDiscoveryComponents with LagomKafkaComponents

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new BookApplication(context) with LagomDevModeComponents with LagomKafkaComponents

  override def describeService = Some(readDescriptor[BookService])
}

abstract class BookApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[BookService](wire[BookServiceImpl])

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry: JsonSerializerRegistry = BookSerializerRegistry

  clusterSharding.init(
    Entity(BookEntity.typeKey) { entityContext =>
      BookEntity(entityContext)
    }
  )

  val bookService: BookServiceGrpc = wire[BookServiceGrpc]
  val zioEnvironment: ZioEnvironment = wire[ZioEnvironment]

  zioEnvironment.run()
}
