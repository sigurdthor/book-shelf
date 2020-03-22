package org.sigurdthor.graphql

import org.sigurdthor.bookshelf.grpc.bookservice.AddBookResponse
import org.sigurdthor.bookshelf.grpc.bookservice.ZioBookservice.BookServiceClient
import zio.RIO

object model {

  case class Queries()

  case class Mutations(addBook: AddBookArgs => RIO[BookServiceClient, AddBookResponse])

  case class AddBookArgs(isbn: String, title: String, authors: List[String], description: String)

}
