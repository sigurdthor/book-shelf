package graphql

import org.sigurdthor.bookshelf.grpc.BookServiceClient

trait Services {

  def bookService: BookServiceClient

}
