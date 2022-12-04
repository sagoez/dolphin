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

  def withDeadline(long: Long): WriteOptions =
    new WriteOptions {
      override def get: Try[AppendToStreamOptions] = self.get.map(_.deadline(long))
    }

  def withExpectedRevision(expectedRevision: ExpectedRevision): WriteOptions =
    new WriteOptions {
      override def get: Try[AppendToStreamOptions] = self.get.map(_.expectedRevision(expectedRevision))
    }

  def withLeaderRequired(isRequired: Boolean): WriteOptions =
    new WriteOptions {
      override def get: Try[AppendToStreamOptions] = self.get.map(_.requiresLeader(isRequired))
    }

}

object WriteOptions {

  def default: WriteOptions = new WriteOptions {}

}
