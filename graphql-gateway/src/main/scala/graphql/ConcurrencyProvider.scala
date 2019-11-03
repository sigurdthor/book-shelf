package graphql

import scala.concurrent.ExecutionContext

trait ConcurrencyProvider {

  implicit def executor: ExecutionContext

}
