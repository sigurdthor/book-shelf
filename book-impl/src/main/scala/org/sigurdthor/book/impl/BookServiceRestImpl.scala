package org.sigurdthor.book.impl

import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import org.sigurdthor.book.api.{AddBookRequest, AddBookResponse, BookService}

/**
  * Implementation of the BookshelfService.
  */
class BookServiceRestImpl(persistentEntityRegistry: PersistentEntityRegistry) extends BookService {
  override def addBook: ServiceCall[AddBookRequest, AddBookResponse] = ???
}
