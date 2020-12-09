package org.sigurdthor.book.impl

import akka.actor.testkit.typed.scaladsl.{LogCapturing, ScalaTestWithActorTestKit}
import akka.persistence.typed.PersistenceId
import org.scalatest.wordspec.AnyWordSpecLike
import org.sigurdthor.book.api.domain.model.{Author, Description, Title}
import org.sigurdthor.book.domain.BookEntity
import org.sigurdthor.book.domain.commands.{AddBook, AddBookReply, BookAddedReply, BookNotFound, BookReply, GetBook, GetBookReply}

import java.util.UUID

class BookshelfEntitySpec
    extends ScalaTestWithActorTestKit(s"""
                                                                  |akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
                                                                  |akka.persistence.snapshot-store.plugin = "akka.persistence.snapshot-store.local"
                                                                  |akka.persistence.snapshot-store.local.dir = "target/snapshot-${UUID
                                           .randomUUID()
                                           .toString}"
                                                                  |""".stripMargin)
    with AnyWordSpecLike
    with LogCapturing {

  private lazy val randomId: String = UUID.randomUUID().toString

  "Bookshelf" must {

    "add book" in {
      val probe      = createTestProbe[AddBookReply]()
      val bookEntity = spawn(BookEntity(PersistenceId("BookEntity", randomId)))
      bookEntity ! AddBook(Title("Test book"),
                           Seq(Author("John")),
                           Description("test book"),
                           probe.ref)

      probe.expectMessageType[BookAddedReply]
    }

    "get book" in {
      val probe      = createTestProbe[GetBookReply]()
      val bookEntity = spawn(BookEntity(PersistenceId("BookEntity", randomId)))
      bookEntity ! GetBook(probe.ref)

      probe.expectMessageType[BookReply]
    }

    "fail on get book with unknown id" in {
      val probe      = createTestProbe[GetBookReply]()
      val bookEntity = spawn(BookEntity(PersistenceId("BookEntity", UUID.randomUUID().toString)))
      bookEntity ! GetBook(probe.ref)

      probe.expectMessageType[BookNotFound.type]
    }
  }
}
