// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.option

import scala.util.Try

import com.eventstore.dbclient.ReadStreamOptions

// Should I manage credentials here?
sealed abstract case class ReadOptions private () extends Product with Serializable {
  self =>

  /** A length of time (in milliseconds) to use for gRPC deadlines.
    * @param durationInMs
    */
  def withDeadline(durationInMs: Long): ReadOptions =
    new ReadOptions {
      override def get: Try[ReadStreamOptions] = self.get.map(_.deadline(durationInMs))
    }

  /** The maximum event count EventStoreDB will return.
    */
  def withMaxCount(count: Long): ReadOptions =
    new ReadOptions {
      override def get: Try[ReadStreamOptions] = self.get.map(_.maxCount(count))
    }

  protected[dolphin] def get: Try[ReadStreamOptions] = Try(ReadStreamOptions.get())

  /** Reads stream in revision-ascending order.
    */
  def forward: ReadOptions =
    new ReadOptions {
      override def get: Try[ReadStreamOptions] = self.get.map(_.forwards)
    }

  /** Reads stream in revision-descending order.
    */
  def backward: ReadOptions =
    new ReadOptions {
      override def get: Try[ReadStreamOptions] = self.get.map(_.backwards)
    }

  /** Starts from the end of the stream.
    */
  def fromEnd: ReadOptions =
    new ReadOptions {
      override def get: Try[ReadStreamOptions] = self.get.map(_.fromEnd())
    }

  /** Starts from the beginning of the stream.
    */
  def fromStart: ReadOptions =
    new ReadOptions {
      override def get: Try[ReadStreamOptions] = self.get.map(_.fromStart())
    }

  /** If true, requires the request to be performed by the leader of the cluster.
    * @param isRequired
    */
  def withLeaderRequired(isRequired: Boolean): ReadOptions =
    new ReadOptions {
      override def get: Try[ReadStreamOptions] = self.get.map(_.requiresLeader(isRequired))
    }

  /** Starts from the given event revision.
    */
  def withRevision(revision: Long): ReadOptions =
    new ReadOptions {
      override def get: Try[ReadStreamOptions] = self.get.map(_.fromRevision(revision))
    }

}

object ReadOptions {

  /** Reads only the first event in the stream.
    *
    * @return
    *   a new [[ReadOptions]] instance
    */
  def default: ReadOptions = new ReadOptions {}

}
