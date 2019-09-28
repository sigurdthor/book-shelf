import cats.effect.{IO, IOApp}
import cats.implicits._
import io.circe.Json
import io.circe.jawn.parse
import io.circe.optics.JsonPath.root
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.{HttpApp, HttpRoutes}
import sangria.ast.Document
import sangria.execution.{ErrorWithResolver, QueryAnalysisError}
import sangria.marshalling.circe._
import sangria.parser.QueryParser
import utils.GqlUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

trait Routes extends GqlUtils { self: IOApp =>

  lazy val graphqlServer: HttpApp[IO] =
    Router(
      "/api" -> graphqlService,
    ).orNotFound

  lazy val graphqlService = HttpRoutes
    .of[IO] {
      case request@POST -> Root / "graphql" =>
        request.as[Json].flatMap { body ⇒
          val query = root.query.string.getOption(body)
          val operationName = root.operationName.string.getOption(body)
          val variablesStr = root.variables.string.getOption(body)

          def execute = query.map(QueryParser.parse(_)) match {
            case Some(Success(ast)) ⇒
              variablesStr.map(parse) match {
                case Some(Left(error)) ⇒ Future.successful(BadRequest(formatError(error)))
                case Some(Right(json)) ⇒ executeGraphQL(ast, operationName, json)
                case None ⇒ executeGraphQL(ast, operationName, root.variables.json.getOption(body) getOrElse Json.obj())
              }
            case Some(Failure(error)) ⇒ Future.successful(BadRequest(formatError(error)))
            case None ⇒ Future.successful(BadRequest(formatError("No query to execute")))
          }

          IO.fromFuture(IO(execute)).flatten
        }
    }

  private def executeGraphQL(query: Document, operationName: Option[String], variables: Json) =
    runGraphQL(query, operationName, variables)
      .map(Ok(_))
      .recover {
        case error: QueryAnalysisError ⇒ BadRequest(error.resolveError)
        case error: ErrorWithResolver ⇒ InternalServerError(error.resolveError)
      }

}
