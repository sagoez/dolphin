// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.option

import scala.util.Try

import com.eventstore.dbclient.{AppendToStreamOptions, ExpectedRevision}

// Should I manage credentials here?
sealed abstract case class WriteOptions private () extends Product with Serializable {
  self =>

  def withExpectedRevision(expectedRevision: Long): WriteOptions =
    new WriteOptions {
      override def get: Try[AppendToStreamOptions] = self.get.map(_.expectedRevision(expectedRevision))
    }

  def get: Try[AppendToStreamOptions] = Try(AppendToStreamOptions.get())

  /** A length of time (in milliseconds) to use for gRPC deadlines.
    * @param durationInMs
    */
  def withDeadline(durationInMs: Long): WriteOptions =
    new WriteOptions {
      override def get: Try[AppendToStreamOptions] = self.get.map(_.deadline(durationInMs))
    }

  /** Asks the server to check that the stream receiving is at the given expected version.
    *
    * @param revision
    *   expected revision.
    */
  def withExpectedRevision(expectedRevision: ExpectedRevision): WriteOptions =
    new WriteOptions {
      override def get: Try[AppendToStreamOptions] = self.get.map(_.expectedRevision(expectedRevision))
    }

  /** If true, requires the request to be performed by the leader of the cluster.
    * @param value
    */
  def withLeaderRequired(isRequired: Boolean): WriteOptions =
    new WriteOptions {
      override def get: Try[AppendToStreamOptions] = self.get.map(_.requiresLeader(isRequired))
    }

}

object WriteOptions {

  def default: WriteOptions = new WriteOptions {}

}
