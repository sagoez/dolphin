// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.outcome

import java.time.Instant
import java.util.UUID

import scala.jdk.OptionConverters.*

import dolphin.concurrent.Position
import dolphin.concurrent.Position.*

import cats.Applicative
import com.eventstore.dbclient
import com.eventstore.dbclient.ResolvedEvent

sealed trait ResolvedEventOutcome[F[_]] {

  /** The event's payload data. */
  def getEventData: F[Array[Byte]]

  /** The event's content type. Could be <i>application/json</i> or <i>application/octet-stream</i>. */
  def getEventContentType: F[String]

  /** The event's creation date. */
  def getEventCreateDate: F[Instant]

  /** The event's unique identifier. */
  def getEventId: F[UUID]

  /** The event's type. */
  def getEventType: F[String]

  /** The stream's unique identifier. */
  def getStreamId: F[String]

  /** The stream's revision. */
  def getStreamRevision: F[Long]

  /** The event's user metadata. */
  def getUserMetadata: F[Array[Byte]]

  /** The event's payload data if the event is a link event. */
  def getLinkEventData: F[Array[Byte]]

  /** The event's content type if the event is a link event. */
  def getLinkEventContentType: F[String]

  /** The event's creation date if the event is a link event. */
  def getLinkEventCreateDate: F[Instant]

  /** The event's unique identifier if the event is a link event. */
  def getLinkEventId: F[UUID]

  /** The event's type if the event is a link event. */
  def getLinkEventType: F[String]

  /** The stream's unique identifier if the event is a link event. */
  def getLinkStreamId: F[String]

  /** The stream's revision if the event is a link event. */
  def getLinkStreamRevision: F[Long]

  /** The event's user metadata if the event is a link event. */
  def getLinkUserMetadata: F[Array[Byte]]

  /** The transaction log position of the event. */
  def getPosition: F[Option[Position]]

  def getResolvedEventUnsafe: ResolvedEvent
}

object ResolvedEventOutcome {

  private[dolphin] def make[F[_]: Applicative](
    ctx: dbclient.ResolvedEvent
  ): ResolvedEventOutcome[F] =
    new ResolvedEventOutcome[F] {

      private def getRecordedEvent       = ctx.getOriginalEvent
      private def getLinkedRecordedEvent = ctx.getLink

      /** The event's payload data.
        */
      def getEventData: F[Array[Byte]] = Applicative[F].pure(getRecordedEvent.getEventData)

      /** The event's content type. Could be <i>application/json</i> or <i>application/octet-stream</i>.
        */
      def getEventContentType: F[String] = Applicative[F].pure(getRecordedEvent.getContentType)

      /** The event's creation date.
        */
      def getEventCreateDate: F[Instant] = Applicative[F].pure(getRecordedEvent.getCreated)

      /** The event's unique identifier.
        */
      def getEventId: F[UUID] = Applicative[F].pure(getRecordedEvent.getEventId)

      /** The event's type.
        */
      def getEventType: F[String] = Applicative[F].pure(getRecordedEvent.getEventType)

      /** The stream's unique identifier.
        */
      def getStreamId: F[String] = Applicative[F].pure(getRecordedEvent.getStreamId)

      /** The stream's revision.
        */
      def getStreamRevision: F[Long] = Applicative[F].pure(getRecordedEvent.getRevision)

      /** The event's user metadata.
        */
      def getUserMetadata: F[Array[Byte]] = Applicative[F].pure(getRecordedEvent.getUserMetadata)

      /** The event's payload data if the event is a link event.
        */
      def getLinkEventData: F[Array[Byte]] = Applicative[F].pure(getLinkedRecordedEvent.getEventData)

      /** The event's content type. Could be <i>application/json</i> or <i>application/octet-stream</i>.
        */
      def getLinkEventContentType: F[String] = Applicative[F].pure(getLinkedRecordedEvent.getContentType)

      /** The event's creation date if the event is a link event.
        */
      def getLinkEventCreateDate: F[Instant] = Applicative[F].pure(getLinkedRecordedEvent.getCreated)

      /** The event's unique identifier if the event is a link event.
        */
      def getLinkEventId: F[UUID] = Applicative[F].pure(getLinkedRecordedEvent.getEventId)

      /** The event's type if the event is a link event.
        */
      def getLinkEventType: F[String] = Applicative[F].pure(getLinkedRecordedEvent.getEventType)

      /** The stream's unique identifier if the event is a link event.
        */
      def getLinkStreamId: F[String] = Applicative[F].pure(getLinkedRecordedEvent.getStreamId)

      /** The stream's revision if the event is a link event.
        */
      def getLinkStreamRevision: F[Long] = Applicative[F].pure(getLinkedRecordedEvent.getRevision)

      /** The event's user metadata if the event is a link event.
        */
      def getLinkUserMetadata: F[Array[Byte]] = Applicative[F].pure(getLinkedRecordedEvent.getUserMetadata)

      /** The transaction log position of the event.
        */
      def getPosition: F[Option[Position]] = Applicative[F].pure(ctx.getPosition.toScala.map(_.toScala))

      /** Gives access to the underlying [[ResolvedEvent]]. */
      def getResolvedEventUnsafe: ResolvedEvent = ctx
    }
}
