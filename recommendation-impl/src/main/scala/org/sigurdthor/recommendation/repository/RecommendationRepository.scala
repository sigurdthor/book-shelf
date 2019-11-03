package org.sigurdthor.recommendation.repository


import java.sql.Connection

import akka.NotUsed
import net.bitnine.agensgraph.graph.Vertex
import net.bitnine.agensgraph.util.JsonbUtil
import org.postgresql.util.PSQLException
import org.sigurdthor.book.api.domain.model.{Author, ISBN, Title}
import org.sigurdthor.bookshelf.grpc.Recommendation
import org.sigurdthor.recommendation.utils.Implicits._
import zio.ZIO
import zio.blocking._

trait RecommendationRepository {
  def getRecommendations(isbn: String): ZIO[Blocking, Throwable, List[Recommendation]]

  def addBook(isbn: ISBN, title: Title, authors: Seq[Author]): ZIO[Blocking, Throwable, NotUsed]
}


class RecommendationRepositoryLive(val connection: Connection) extends RecommendationRepository {
  override def getRecommendations(isbn: String): ZIO[Blocking, Throwable, List[Recommendation]] =
    effectBlocking(executeGetRecommendations(isbn)).catchSome {
      case _: PSQLException => ZIO.succeed(List())
    }

  override def addBook(isbn: ISBN, title: Title, authors: Seq[Author]): ZIO[Blocking, Throwable, NotUsed] =
    effectBlocking(executeAddBook(isbn, title, authors))
      .map(_ => NotUsed)


  private def executeAddBook(isbn: ISBN, title: Title, authors: Seq[Author]) = {
    val pstmt = connection.prepareStatement("SET graph_path=agens_graph;" + "CREATE (:Book ?)-[:WRITTEN_BY]->(:Author ?)")
    val book = JsonbUtil.createObjectBuilder.add("isbn", isbn.value).add("title", title.value).build
    val author = JsonbUtil.createObjectBuilder.add("name", authors.head.value).build
    pstmt.setObject(1, book)
    pstmt.setObject(2, author)
    pstmt.execute()
  }

  private def executeGetRecommendations(isbn: String): List[Recommendation] = {
    val stmt = connection.prepareStatement("SET graph_path=agens_graph;" + "MATCH (book:Book)-[:WRITTEN_BY]->(author)<-[:WRITTEN_BY]-(books:Book) WHERE book.isbn = ? RETURN books")
    stmt.setString(1, isbn)
    stmt.executeQuery.toStream
      .map { result =>
        val book = result.getObject(1).asInstanceOf[Vertex]
        Recommendation(book.getString("isbn"), book.getString("title"), Seq())
      }
      .take(10)
      .toList
  }
}
