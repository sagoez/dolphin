package dolphin.option

import scala.util.Try

import com.eventstore.dbclient.ReadStreamOptions

// Should I manage credentials here?
sealed abstract case class ReadOptions private () extends Product with Serializable {
  self =>

  def withDeadline(long: Long): ReadOptions =
    new ReadOptions {
      override def get: Try[ReadStreamOptions] = self.get.map(_.deadline(long))
    }

  def withMaxCount(count: Long): ReadOptions =
    new ReadOptions {
      override def get: Try[ReadStreamOptions] = self.get.map(_.maxCount(count))
    }

  private[dolphin] def get: Try[ReadStreamOptions] = Try(ReadStreamOptions.get())

  def forward: ReadOptions =
    new ReadOptions {
      override def get: Try[ReadStreamOptions] = self.get.map(_.forwards)
    }

  def backward: ReadOptions =
    new ReadOptions {
      override def get: Try[ReadStreamOptions] = self.get.map(_.backwards)
    }

  def fromEnd: ReadOptions =
    new ReadOptions {
      override def get: Try[ReadStreamOptions] = self.get.map(_.fromEnd())
    }

  def fromStart: ReadOptions =
    new ReadOptions {
      override def get: Try[ReadStreamOptions] = self.get.map(_.fromStart())
    }

  def withLeaderRequired(isRequired: Boolean): ReadOptions =
    new ReadOptions {
      override def get: Try[ReadStreamOptions] = self.get.map(_.requiresLeader(isRequired))
    }

  def withRevision(revision: Long): ReadOptions =
    new ReadOptions {
      override def get: Try[ReadStreamOptions] = self.get.map(_.fromRevision(revision))
    }

}

object ReadOptions {

  /** Reads only the first event in the stream due to the revision being set to 0.
    *
    * @return
    *   a new [[ReadOptions]] instance
    */
  def default: ReadOptions = new ReadOptions {}.withRevision(0).withMaxCount(100).backward

}
