package org.sigurdthor.recommendation.repository

import com.sksamuel.elastic4s.{ElasticClient, ElasticProperties}
import com.sksamuel.elastic4s.http.JavaClient
import org.sigurdthor.recommendation.config.Elasticsearch
import org.sigurdthor.lib.FutureOps._
import zio._

import scala.concurrent.ExecutionContext

trait ElasticConnection {

  implicit def ec: ExecutionContext

  def initialize(host: String, port: String): Task[ElasticClient] = {
    import com.sksamuel.elastic4s.ElasticDsl._

    val esClient: ElasticClient = ElasticClient(
      JavaClient(ElasticProperties(s"http://${sys.env.getOrElse("ES_HOST", host)}:${sys.env.getOrElse("ES_PORT", port)}"))
    )

    esClient
      .execute {
        createIndex("recommendations")
          .mapping(
            properties(
              keywordField("isbn"),
              keywordField("title").copyTo("all"),
              keywordField("authors").copyTo("all"),
              textField("all")
            )
          )
      }
      .map(_ => esClient)
      .toTask
  }

  def elasticClientLayer(cfg: Elasticsearch): ZLayer[Any, Throwable, Has[ElasticClient]] =
    ZLayer.fromAcquireRelease(initialize(cfg.host, cfg.port))(c => UIO(c.close()))
}
