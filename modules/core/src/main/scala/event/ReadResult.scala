// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.event

import java.time.Instant
import java.util.UUID
import java.util.concurrent.CompletableFuture

import scala.jdk.CollectionConverters.*
import scala.util.Try

import cats.effect.kernel.Async
import cats.syntax.applicative.*
import cats.syntax.functor.*
import com.eventstore.dbclient.{ReadResult as EventStoreReadResult, _}
import fs2.Stream
import org.typelevel.log4cats.Logger

sealed abstract case class ReadResult[F[_]: Async] private (
  private val completableFuture: CompletableFuture[EventStoreReadResult]
) { self =>

  private def get = Async[F].fromCompletableFuture[EventStoreReadResult](completableFuture.pure[F])

  /** Returns all the events of the read operation.
    */
  private def getResolvedEvents: Stream[F, ResolvedEvent] = Stream.evalSeq(get.map(_.getEvents.asScala.toList))

  /** Returns the event that was read or which triggered the subscription. If the resolved event represents a link
    * event, the link will be the original event, otherwise it will be the event.
    */
  private def getRecordedEvent: Stream[F, RecordedEvent] = getResolvedEvents.map(_.getOriginalEvent())

  /** The event's payload data.
    */
  def getEventData: Stream[F, Array[Byte]] = getRecordedEvent.map(_.getEventData())

  /** The event's content type. Could be <i>application/json</i> or <i>application/octet-stream</i>.
    */
  def getEventContentType: Stream[F, String] = getRecordedEvent.map(_.getContentType())

  /** When the event was created.
    */
  def getEventCreateDate: Stream[F, Instant] = getRecordedEvent.map(_.getCreated())

  /** The event's unique identifier.
    */
  def getEventId: Stream[F, UUID] = getRecordedEvent.map(_.getEventId())

  /** The event's type.
    */
  def getEventType: Stream[F, String] = getRecordedEvent.map(_.getEventType())

  /** The stream that event belongs to.
    */
  def getStreamId: Stream[F, String] = getRecordedEvent.map(_.getStreamId())

  /** The event's transaction log position.
    */
  def getPosition: Stream[F, Position] = getRecordedEvent.map(_.getPosition())

  /** The event's stream revision number.
    */
  def getRevision: Stream[F, Long] = getRecordedEvent.map(_.getRevision())

  /** When reading from a regular stream, returns the first event revision number of the stream.
    */
  def getFirstStreamPosition: F[Long] = get.map(_.getFirstStreamPosition())

  /** When reading from a regular stream, returns the last event revision number of the stream.
    */
  def getLastStreamPosition: F[Long] = get.map(_.getLastStreamPosition())

  /** When reading from \$all stream, returns the last event position.
    * @return
    *   None if reading from a regular stream.
    */
  def getLastAllStreamPosition: F[Option[Position]] = get.map(value => Try(value.getLastAllStreamPosition()).toOption)

}

private[dolphin] object ReadResult {

  def fromCompletableFuture[F[_]: Async](result: CompletableFuture[EventStoreReadResult]): ReadResult[F] =
    new ReadResult[F](result) {}

  def fromEventReadResult[F[_]: Async](result: EventStoreReadResult): ReadResult[F] =
    new ReadResult[F](CompletableFuture.completedFuture(result)) {}

  implicit class ReadResultOps[F[_]: Async: Logger](val writeResult: CompletableFuture[EventStoreReadResult]) {

    import cats.syntax.applicativeError.*
    import cats.syntax.flatMap.*
    import cats.syntax.apply.*

    def toSafeAttempt: F[ReadResult[F]] = Async[F].fromCompletableFuture(writeResult.pure[F]).attempt.flatMap {
      case Left(error)   => Logger[F].error(error)("Failed to read from EventStore") *> Async[F].raiseError(error)
      case Right(result) => ReadResult.fromEventReadResult(result).pure[F]
    }
  }

}
