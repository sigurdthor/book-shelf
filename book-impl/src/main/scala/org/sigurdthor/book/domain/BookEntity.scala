package org.sigurdthor.book.domain

import java.time.OffsetDateTime

import akka.actor.typed.{ActorRef, Behavior}
import akka.cluster.sharding.typed.scaladsl.{EntityContext, EntityTypeKey}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.Effect.reply
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, ReplyEffect, RetentionCriteria}
import com.lightbend.lagom.scaladsl.persistence.AkkaTaggerAdapter
import org.sigurdthor.book.api.domain.model._
import org.sigurdthor.book.domain.commands._
import org.sigurdthor.book.domain.events.{BookAdded, BookEvent}
import org.sigurdthor.book.lib.Transformations._
import play.api.libs.json.{Format, Json}


object BookEntity {

  val empty: BookEntity = BookEntity()

  val typeKey: EntityTypeKey[Command] = EntityTypeKey[Command]("BookEntity")

  def apply(persistenceId: PersistenceId): EventSourcedBehavior[Command, BookEvent, BookEntity] = {
    EventSourcedBehavior
      .withEnforcedReplies[Command, BookEvent, BookEntity](
        persistenceId = persistenceId,
        emptyState = BookEntity.empty,
        commandHandler = (book, cmd) => book.applyCommand(cmd, persistenceId),
        eventHandler = (book, evt) => book.applyEvent(evt)
      )
  }

  def apply(entityContext: EntityContext[Command]): Behavior[Command] =
    apply(PersistenceId(entityContext.entityTypeKey.name, entityContext.entityId))
      .withTagger(AkkaTaggerAdapter.fromLagom(entityContext, BookEvent.Tag))
      .withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 100, keepNSnapshots = 2))


  implicit val bookEntityFormat: Format[BookEntity] = Json.format
}


final case class BookEntity(title: Option[Title] = None,
                            authors: Seq[Author] = Seq(),
                            description: Option[Description] = None) {

  def exists: Boolean = title.isDefined

  def applyCommand(cmd: Command, persistenceId: PersistenceId): ReplyEffect[BookEvent, BookEntity] =
    if (exists) {
      cmd match {
        case AddBook(_, _, _, replyTo) => reply(replyTo)(BookAlreadyExists)
        case GetBook(replyTo) => CommandHandler.onGetBook(persistenceId, replyTo)
      }
    } else {
      cmd match {
        case AddBook(title, authors, description, replyTo) => CommandHandler.onAddBook(ISBN(persistenceId.id), title, authors, description, replyTo)
        case GetBook(replyTo) => reply(replyTo)(BookNotFound)
      }
    }

  def applyEvent(evt: BookEvent): BookEntity =
    evt match {
      case BookAdded(_, title, authors, description) => EventHandler.onBookAdded(title, authors, description)
    }

  private object CommandHandler {

    def onAddBook(isbn: ISBN,
                  title: Title,
                  authors: Seq[Author],
                  description: Description,
                  replyTo: ActorRef[AddBookReply]
                 ): ReplyEffect[BookEvent, BookEntity] = {
      Effect
        .persist(BookAdded(
          isbn,
          title,
          authors,
          description
        ))
        .thenReply(replyTo)(_ => BookAddedReply(OffsetDateTime.now()))
    }

    def onGetBook(persistenceId: PersistenceId, replyTo: ActorRef[GetBookReply]): ReplyEffect[BookEvent, BookEntity] = {
      reply(replyTo)(BookEntity.this.toReply(ISBN(persistenceId.id)))
    }
  }

  private object EventHandler {

    def onBookAdded(title: Title,
                    authors: Seq[Author],
                    description: Description): BookEntity =
      copy(title = Some(title), authors = authors, description = Some(description))
  }

}
