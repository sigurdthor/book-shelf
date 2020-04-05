package org.sigurdthor.graphql.model

import org.sigurdthor.bookshelf.grpc.bookservice.AddBookRequest
import io.scalaland.chimney.dsl._
import org.sigurdthor.graphql.model.GraphqlEntities.AddBookArgs

object Transformations {

  implicit class ArgsTransformer(args: AddBookArgs) {

    def toRequest: AddBookRequest =
      args
        .into[AddBookRequest]
        .transform
  }

}
