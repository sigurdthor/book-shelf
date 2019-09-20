package org.sigurdthor.book.impl

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.Materializer

import scala.concurrent.Future
import org.sigurdthor.bookshelf.grpc.{AbstractBookServiceRouter, AddBookRequest, AddBookResponse}

class BookServiceGrpcImpl(mat: Materializer, system: ActorSystem)
  extends AbstractBookServiceRouter(mat, system) {
  override def addBook(req: AddBookRequest): Future[AddBookResponse] =
    Future.successful(AddBookResponse(UUID.randomUUID().toString, "book"))
}
