package org.sigurdthor.graphql.service

import caliban.CalibanError.{ExecutionError, ParsingError, ValidationError}
import caliban.ResponseValue.ObjectValue
import caliban.Value.StringValue
import caliban.{CalibanError, GraphQLInterpreter}
import io.grpc.StatusException

object ErrorHandler {

  def withErrorCodeExtensions[R](
                                  interpreter: GraphQLInterpreter[R, Throwable]
                                ): GraphQLInterpreter[R, CalibanError] = interpreter.mapError {
    case err@ExecutionError(_, _, _, Some(error: StatusException), _) =>
      err.copy(extensions = Some(ObjectValue(List(("errorCode" -> StringValue(String.valueOf(error.getStatus.getCode.value())))))))
    case err: ExecutionError =>
      err.copy(extensions = Some(ObjectValue(List(("errorCode", StringValue("EXECUTION_ERROR"))))))
    case err: ValidationError =>
      err.copy(extensions = Some(ObjectValue(List(("errorCode", StringValue("VALIDATION_ERROR"))))))
    case err: ParsingError =>
      err.copy(extensions = Some(ObjectValue(List(("errorCode", StringValue("PARSING_ERROR"))))))
  }
}
