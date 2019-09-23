package org.sigurdthor.book.domain

import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import org.sigurdthor.book.domain.commands.AddBook
import org.sigurdthor.book.domain.events.BookAdded

import scala.collection.immutable.Seq

object BookSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[AddBook],
    JsonSerializer[BookAdded],
    JsonSerializer[BookState]
  )
}
