// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.concurrent

import com.eventstore.dbclient

sealed trait ExpectedRevision

object ExpectedRevision {

  case object Any          extends ExpectedRevision
  case object NoStream     extends ExpectedRevision
  case object StreamExists extends ExpectedRevision

  final case class Exact(value: Long) extends ExpectedRevision

  final implicit class ExpectedRevisionOps(val self: ExpectedRevision) extends AnyVal {

    private[dolphin] def toJava: dbclient.ExpectedRevision =
      self match {
        case Any          => dbclient.ExpectedRevision.any()
        case NoStream     => dbclient.ExpectedRevision.noStream()
        case StreamExists => dbclient.ExpectedRevision.streamExists()
        case Exact(value) => dbclient.ExpectedRevision.expectedRevision(value)
      }
  }

  final implicit class DbClientExpectedRevisionOps(val rev: dbclient.ExpectedRevision) extends AnyVal {

    // See https://github.com/EventStore/EventStoreDB-Client-Java/blob/trunk/db-client-java/src/main/java/com/eventstore/dbclient/ExpectedRevision.java
    private[dolphin] def toScala: ExpectedRevision =
      rev.toRawLong() match {
        case -1L         => ExpectedRevision.NoStream
        case -2L         => ExpectedRevision.Any
        case -4L         => ExpectedRevision.StreamExists
        // Gracefully handle the case where the value is not greater than -1
        case v if v > -1 => ExpectedRevision.Exact(rev.toRawLong())
        case _           => ExpectedRevision.NoStream
      }
  }
}
