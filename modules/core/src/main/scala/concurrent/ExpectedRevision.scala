// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.concurrent

import java.lang.reflect.Field

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

    // Bit of unsafe code here, but it's the only way (I know) to get the value of the private field
    private[dolphin] def toScala: ExpectedRevision =
      if (rev != dbclient.ExpectedRevision.any()) {
        if (rev == dbclient.ExpectedRevision.noStream()) {
          ExpectedRevision.NoStream
        } else if (rev == dbclient.ExpectedRevision.streamExists()) {
          ExpectedRevision.StreamExists
        } else {
          // If we can't get the value, we should throw as it is unsafe to continue writing with the wrong expected revision value
          val fields: Array[Field] = rev.getClass.getDeclaredFields.collect {
            case field if field.getName == "version" && field.getType == classOf[Long] =>
              field.setAccessible(true)
              field
          }

          val value = fields
            .map(_.getLong(rev))
            .headOption
            .getOrElse(throw new RuntimeException("Could not get the value of the expected revision"))

          ExpectedRevision.Exact(value)
        }
      } else {
        ExpectedRevision.Any
      }
  }
}
