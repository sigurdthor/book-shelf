package org.sigurdthor.recommendation.repository


import java.sql.{Connection, DriverManager}

import akka.NotUsed
import org.sigurdthor.book.api.domain.model.{Author, ISBN, Title}
import org.sigurdthor.bookshelf.grpc.Recommendation
import org.sigurdthor.recommendation.utils.ZioContext.Environment
import zio.ZIO
import zio.clock.Clock


/**
  * External services
  */
trait Repositories extends Clock {
  val recommendationsRepository: RecommendationRepository
}

object Repositories {

  def addBook(isbn: ISBN, title: Title, authors: Seq[Author]): ZIO[Environment, Throwable, NotUsed] =
    ZIO.accessM[Environment](_.recommendationsRepository.addBook(isbn, title, authors))

  def retrieveRecommendations(isbn: String): ZIO[Environment, Throwable, List[Recommendation]] =
    ZIO.accessM[Environment](_.recommendationsRepository.getRecommendations(isbn))

}

class RepositoriesLive extends Repositories with Clock.Live {

  Class.forName("net.bitnine.agensgraph.Driver")

  val connectionString = "jdbc:agensgraph://127.0.0.1:5432/agens"
  val username = "agens"
  val password = ""
  val connection: Connection = DriverManager.getConnection(connectionString, username, password)

  override val recommendationsRepository: RecommendationRepository = new RecommendationRepositoryLive(connection)
}

