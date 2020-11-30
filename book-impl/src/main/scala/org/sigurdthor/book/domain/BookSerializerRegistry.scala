package org.sigurdthor.book.domain

import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import org.sigurdthor.book.api.domain.model.Book
import org.sigurdthor.book.domain.commands.{AddBookReply, BookAlreadyExists, BookNotFound, GetBookReply}
import org.sigurdthor.book.domain.events.BookAdded

import scala.collection.immutable.Seq

object BookSerializerRegistry extends JsonSerializerRegistry {

  override def serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[GetBookReply],
    JsonSerializer[BookAdded],
    JsonSerializer[BookEntity],
    JsonSerializer[BookAlreadyExists.type],
    JsonSerializer[AddBookReply],
    JsonSerializer[BookNotFound.type],
    JsonSerializer[Book]
  )
}
