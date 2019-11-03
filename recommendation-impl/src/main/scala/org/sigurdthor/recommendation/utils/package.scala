package org.sigurdthor.recommendation

import java.sql.ResultSet

package object utils {

  object Implicits {

    implicit class ResultSetStream(resultSet: ResultSet) {

      def toStream: Stream[ResultSet] = {
        new Iterator[ResultSet] {
          def hasNext = resultSet.next()

          def next() = resultSet
        }.toStream
      }
    }
  }

}
