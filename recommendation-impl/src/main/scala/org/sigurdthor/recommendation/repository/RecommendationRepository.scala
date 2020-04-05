package org.sigurdthor.recommendation.repository


import akka.NotUsed
import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.circe._
import com.sksamuel.elastic4s.requests.common.RefreshPolicy
import com.sksamuel.elastic4s.zio.instances._
import org.sigurdthor.book.api.domain.model.{Author, ISBN, Title}
import org.sigurdthor.bookshelf.grpc.recommendationservice.Recommendation
import org.sigurdthor.recommendation.repository.RecommendationRepository.RecommendationRepo.Service
import zio.{Has, Task, ZLayer}


object RecommendationRepository {

  import com.sksamuel.elastic4s.ElasticDsl._
  import org.sigurdthor.recommendation.domain.codecs.CirceCodecs._

  type RecommendationRepo = Has[RecommendationRepo.Service]


  object RecommendationRepo {

    trait Service {
      def findRecommendations(query: String): Task[Seq[Recommendation]]

      def addBook(isbn: ISBN, title: Title, authors: Seq[Author]): Task[NotUsed]
    }

  }

  val live: ZLayer[Has[ElasticClient], Nothing, RecommendationRepo] = ZLayer.fromFunction { hasClient =>

    new Service {
      override def findRecommendations(query: String): Task[Seq[Recommendation]] = {
        hasClient.get.execute {
          search("recommendations").matchQuery("all", query)
        }.map(_.result.to[Recommendation])
      }

      override def addBook(isbn: ISBN, title: Title, authors: Seq[Author]): Task[NotUsed] = {
        hasClient.get.execute {
          bulk(
            indexInto("recommendations").fields(
              "isbn" -> isbn.value,
              "title" -> title.value,
              "authors" -> authors.map(_.value).mkString(" "))
          ).refresh(RefreshPolicy.IMMEDIATE)
        }.as(NotUsed)
      }
    }
  }
}




