package graphql

import org.sigurdthor.bookshelf.grpc.{AddBookRequest, AddBookResponse}
import sangria.macros.derive.deriveObjectType
import sangria.schema.{Argument, Field, ListInputType, ObjectType, OptionInputType, StringType, fields}

import scala.concurrent.Future

trait Mutations { self: Services =>

  implicit val addBookResponseType = deriveObjectType[SecureContext, AddBookResponse]()

  private val isbnArg = Argument("isbn", StringType)
  private val titleArg = Argument("title", StringType)
  private val authorsArg = Argument("authors", ListInputType(StringType))
  private val descriptionArg = Argument("description", StringType)

  lazy val MutationType = ObjectType(
    "Mutation",
    () =>
      fields[SecureContext, Unit](
        Field(
          "addBook",
          addBookResponseType,
          arguments = isbnArg :: titleArg :: authorsArg :: descriptionArg :: Nil,
          resolve = c =>
            c.ctx.mutations
              .addBook(
                c.arg(isbnArg),
                c.arg(titleArg),
                c.arg(authorsArg),
                c.arg(descriptionArg)
              )
        )))

   def addBook(isbn: String, title: String, authors: Seq[String], description: String): Future[AddBookResponse]  =
     bookService.addBook(AddBookRequest(isbn, title, authors, description))


}
