package org.sigurdthor.book.domain

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import org.sigurdthor.book.api.domain.model._
import org.sigurdthor.book.domain.commands.{AddBook, BookCommand, GetBook}
import org.sigurdthor.book.domain.events.{BookAdded, BookEvent}
import play.api.libs.json.{Format, Json}
import org.sigurdthor.book.lib.Transformations._


class BookEntity extends PersistentEntity {

  override type Command = BookCommand[_]
  override type Event = BookEvent
  override type State = BookState

  override def initialState: BookState = BookState.empty

  override def behavior: Behavior = { _ =>
    Actions()
      .orElse(CommandHandler.addBook)
      .orElse(CommandHandler.getBook)
      .orElse(EventHandler.bookAdded)
  }

  private object CommandHandler {

    def addBook: Actions = Actions().onCommand[AddBook, Done] {
      case (AddBook(title, authors, description), ctx, _) =>
        ctx.thenPersist(
          BookAdded(
            entityId,
            title,
            authors,
            description
          )
        )(
          _ => ctx.reply(Done)
        )
    }

    def getBook: Actions = Actions().onReadOnlyCommand[GetBook.type, Book] {
      case (GetBook, ctx, state) =>
        ctx.reply(state.toBook(entityId))
    }
  }

  private object EventHandler {

    def bookAdded: Actions = Actions().onEvent {
      case (BookAdded(_, title, authors, description), state) =>
        state.copy(title = title, authors = authors, description = description)
    }
  }

}


case class BookState(title: Title, authors: Seq[Author], description: Description)

object BookState {

  def empty = BookState(Title(""), List(), Description(""))

  implicit val format: Format[BookState] = Json.format
}
