package org.sigurdthor.book.domain

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import org.sigurdthor.book.api.domain.model.{Author, Book, Description, ISBN, Title}
import org.sigurdthor.book.domain.commands.{AddBook, BookCommand, GetBookCommand}
import org.sigurdthor.book.domain.events.{BookAdded, BookEvent}
import play.api.libs.json.{Format, Json}


class BookEntity extends PersistentEntity {

  override type Command = BookCommand[_]
  override type Event = BookEvent
  override type State = BookState

  override def initialState: BookState = BookState.empty

  override def behavior: Behavior = { _ =>
    Actions()
      .orElse(CommandHandler.addBook)
      .orElse(EventHandler.bookAdded)
  }

  private object CommandHandler {

    def addBook: Actions = Actions().onCommand[AddBook, Done] {
      case (AddBook(title, authors, description), ctx, _) =>
        ctx.thenPersist(
          BookAdded(
            ISBN(entityId),
            title,
            authors,
            description
          )
        )(
          _ => ctx.reply(Done)
        )
    }

    def getBook: Actions = Actions().onReadOnlyCommand[GetBookCommand.type, Book] {
      case (GetBookCommand, ctx, state) =>
        ctx.reply(Book(ISBN(entityId), state.title, state.authors, state.description))
    }
  }

  private object EventHandler {

    def bookAdded: Actions = Actions().onEvent {
      case (BookAdded(isbn, title, authors, description), state) =>
        state.copy(isbn = isbn, title = title, authors = authors, description = description)
    }
  }

}


case class BookState(isbn: ISBN, title: Title, authors: Seq[Author], description: Description)

object BookState {

  def empty = BookState(ISBN(""), Title(""), List(), Description(""))

  implicit val format: Format[BookState] = Json.format
}
