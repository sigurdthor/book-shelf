package org.sigurdthor.graphql.model

import org.sigurdthor.bookshelf.grpc.bookservice.ZioBookservice.BookServiceClient
import org.sigurdthor.bookshelf.grpc.bookservice.{AddBookResponse, BookResponse}
import zio.RIO


case class Queries(getBook: GetBookArgs => RIO[BookServiceClient, BookResponse])

case class Mutations(addBook: AddBookArgs => RIO[BookServiceClient, AddBookResponse])

case class AddBookArgs(isbn: String, title: String, authors: List[String], description: String)

case class GetBookArgs(isbn: String)

