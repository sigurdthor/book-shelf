package utils

import graphql.{SchemaDefinition, SecureContext}
import io.circe.Json
import sangria.ast.Document
import sangria.execution.Executor
import sangria.marshalling.circe._
import sangria.parser.SyntaxError

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.control.NonFatal

trait GqlUtils {

  def schemaDefinition: SchemaDefinition

  def runGraphQL(query: Document, operationName: Option[String], variables: Json) =
    Executor.execute(schemaDefinition.schema, query, SecureContext(None, schemaDefinition),
      variables = if (variables.isNull) Json.obj() else variables,
      operationName = operationName)

  def formatError(error: Throwable): Json = error match {
    case syntaxError: SyntaxError ⇒
      Json.obj("errors" → Json.arr(
        Json.obj(
          "message" → Json.fromString(syntaxError.getMessage),
          "locations" → Json.arr(Json.obj(
            "line" → Json.fromBigInt(syntaxError.originalError.position.line),
            "column" → Json.fromBigInt(syntaxError.originalError.position.column))))))
    case NonFatal(e) ⇒
      formatError(e.getMessage)
    case e ⇒
      throw e
  }

  def formatError(message: String): Json =
    Json.obj("errors" → Json.arr(Json.obj("message" → Json.fromString(message))))

}
