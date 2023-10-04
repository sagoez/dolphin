// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.outcome

import java.time.Instant
import java.util.UUID

import scala.jdk.CollectionConverters.*

import dolphin.concurrent.Position
import dolphin.concurrent.Position.*

import com.eventstore.dbclient
import com.eventstore.dbclient.{RecordedEvent, ResolvedEvent}
import fs2.Stream

sealed trait Read[F[_]] {

  /** Returns all the events of the read operation.
    */
  def getResolvedEvents: Stream[F, ResolvedEvent]

  /** The event's payload data.
    */
  def getEventData: Stream[F, Array[Byte]]

  /** The event's content type. Could be <i>application/json</i> or <i>application/octet-stream</i>.
    */
  def getEventContentType: Stream[F, String]

  /** When the event was created.
    */
  def getEventCreateDate: Stream[F, Instant]

  /** The event's unique identifier.
    */
  def getEventId: Stream[F, UUID]

  /** The event's type.
    */
  def getEventType: Stream[F, String]

  /** The stream that event belongs to.
    */
  def getStreamId: Stream[F, String]

  /** The event's transaction log position.
    */
  def getPosition: Stream[F, Position]

  /** The event's stream revision number.
    */
  def getRevision: Stream[F, Long]

  /** When reading from a regular stream, returns the first event revision number of the stream.
    */
  def getFirstStreamPosition: Long

  /** When reading from a regular stream, returns the last event revision number of the stream.
    */
  def getLastStreamPosition: Long

  /** When reading from <b>all</b> stream, returns the last event position. Returns null if reading from a regular
    * stream.
    */
  def getLastAllStreamPosition: Option[Position]
}

object Read {

  private[dolphin] def make[F[_]](
    ctx: dbclient.ReadResult
  ): Read[F] =
    new Read[F] {

      /** Returns all the events of the read operation. */
      def getResolvedEvents: Stream[F, ResolvedEvent] = Stream(ctx.getEvents.asScala.toSeq*)

      /** Returns the event that was read or which triggered the subscription. If the resolved event represents a link
        * event, the link will be the original event, otherwise it will be the event.
        */
      private def getRecordedEvent: Stream[F, RecordedEvent] = getResolvedEvents.map(_.getOriginalEvent())

      /** The event's payload data. */
      def getEventData: Stream[F, Array[Byte]] = getRecordedEvent.map(_.getEventData())

      /** The event's content type. Could be <i>application/json</i> or <i>application/octet-stream</i>. */
      def getEventContentType: Stream[F, String] = getRecordedEvent.map(_.getContentType())

      /** When the event was created. */
      def getEventCreateDate: Stream[F, Instant] = getRecordedEvent.map(_.getCreated())

      /** The event's unique identifier. */
      def getEventId: Stream[F, UUID] = getRecordedEvent.map(_.getEventId())

      /** The event's type. */
      def getEventType: Stream[F, String] = getRecordedEvent.map(_.getEventType())

      /** The stream that event belongs to. */
      def getStreamId: Stream[F, String] = getRecordedEvent.map(_.getStreamId())

      /** The event's transaction log position. */
      def getPosition: Stream[F, Position] = getRecordedEvent.map(_.getPosition.toScala)

      /** The event's stream revision number. */
      def getRevision: Stream[F, Long] = getRecordedEvent.map(_.getRevision())

      /** When reading from a regular stream, returns the first event revision number of the stream. */
      def getFirstStreamPosition: Long = ctx.getFirstStreamPosition

      /** When reading from a regular stream, returns the last event revision number of the stream. */
      def getLastStreamPosition: Long = ctx.getLastStreamPosition

      /** When reading from <b>all</b> stream, returns the last event position. */
      def getLastAllStreamPosition: Option[Position] = Option(ctx.getLastAllStreamPosition.toScala)

    }
}
