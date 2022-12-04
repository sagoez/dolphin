package dolphin.event

import java.util.concurrent.CompletableFuture

import scala.jdk.CollectionConverters.*

import cats.effect.kernel.Async
import cats.syntax.applicative.*
import cats.syntax.functor.*
import com.eventstore.dbclient.{ReadResult => EventReadWriteResult}
import org.typelevel.log4cats.Logger
import com.eventstore.dbclient.ResolvedEvent
import com.eventstore.dbclient.RecordedEvent
import java.time.Instant
import java.util.UUID
import com.eventstore.dbclient.Position
import fs2.Stream

sealed abstract case class ReadResult[F[_]: Async] private (
  private val completableFuture: CompletableFuture[EventReadWriteResult]
) { self =>

  private def get = Async[F].fromCompletableFuture[EventReadWriteResult](completableFuture.pure[F])

  private def getResolvedEvents: Stream[F, ResolvedEvent] = Stream.evalSeq(get.map(_.getEvents.asScala.toList))

  private def getRecordedEvent: Stream[F, RecordedEvent] = getResolvedEvents.map(_.getOriginalEvent())

  private def getRecordedEventAt(
    index: Long
  ): F[Option[RecordedEvent]] = getRecordedEvent.drop(index).head.compile.last

  def getEventDataAt(index: Long): F[Option[Array[Byte]]] = getRecordedEventAt(index).map {
    case Some(event) => Some(event.getEventData)
    case None        => None
  }

  def getContentTypeAt(index: Long): F[Option[String]] =
    getRecordedEventAt(index) map {
      case None        => None
      case Some(value) => Some(value.getContentType())
    }

  def getCreateDateAt(index: Long): F[Option[Instant]] =
    getRecordedEventAt(index) map {
      case None        => None
      case Some(value) => Some(value.getCreated())
    }

  def getEventIdAt(index: Long): F[Option[UUID]] =
    getRecordedEventAt(index) map {
      case None        => None
      case Some(value) => Some(value.getEventId())
    }

  def getEventTypeAt(index: Long): F[Option[String]] =
    getRecordedEventAt(index) map {
      case None        => None
      case Some(value) => Some(value.getEventType())
    }

  def getStreamIdAt(index: Long): F[Option[String]] =
    getRecordedEventAt(index) map {
      case None        => None
      case Some(value) => Some(value.getStreamId())
    }

  def getPositionAt(index: Long): F[Option[Position]] =
    getRecordedEventAt(index) map {
      case None        => None
      case Some(value) => Some(value.getPosition())
    }

  def getRevisionAt(index: Long): F[Option[Long]] =
    getRecordedEventAt(index) map {
      case None        => None
      case Some(value) => Some(value.getRevision())
    }

  def getEventData: Stream[F, Array[Byte]] = getRecordedEvent.map(_.getEventData())

  def getEventContentType: Stream[F, String] = getRecordedEvent.map(_.getContentType())

  def getEventCreateDate: Stream[F, Instant] = getRecordedEvent.map(_.getCreated())

  def getEventId: Stream[F, UUID] = getRecordedEvent.map(_.getEventId())

  def getEventType: Stream[F, String] = getRecordedEvent.map(_.getEventType())

  def getStreamId: Stream[F, String] = getRecordedEvent.map(_.getStreamId())

  def getPosition: Stream[F, Position] = getRecordedEvent.map(_.getPosition())

  def getRevision: Stream[F, Long] = getRecordedEvent.map(_.getRevision())

  def getFirstStreamPosition: F[Long] = get.map(_.getFirstStreamPosition())

  def getLastStreamPosition: F[Long] = get.map(_.getLastStreamPosition())

  def getLastAllStreamPosition: F[Position] = get.map(_.getLastAllStreamPosition())

}

private[dolphin] object ReadResult {

  def fromCompletableFuture[F[_]: Async](result: CompletableFuture[EventReadWriteResult]): ReadResult[F] =
    new ReadResult[F](result) {}

  def fromEventReadResult[F[_]: Async](result: EventReadWriteResult): ReadResult[F] =
    new ReadResult[F](CompletableFuture.completedFuture(result)) {}

  implicit class ReadResultOps[F[_]: Async: Logger](val writeResult: CompletableFuture[EventReadWriteResult]) {

    import cats.syntax.applicativeError.*
    import cats.syntax.flatMap.*
    import cats.syntax.apply.*

    def toSafeAttempt: F[ReadResult[F]] = Async[F].fromCompletableFuture(writeResult.pure[F]).attempt.flatMap {
      case Left(error)   => Logger[F].error(error)("Failed to read from EventStore") *> Async[F].raiseError(error)
      case Right(result) => ReadResult.fromEventReadResult(result).pure[F]
    }
  }

}
