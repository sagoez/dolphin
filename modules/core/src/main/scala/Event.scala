// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin

import java.time.Instant
import java.util.UUID

import scala.jdk.OptionConverters.*

import dolphin.concurrent.Position
import dolphin.concurrent.Position.PositionJavaOps

import com.eventstore.dbclient
import com.eventstore.dbclient.ResolvedEvent

sealed trait Event {

  /** The event's payload data. */
  def getEventData: Array[Byte]

  /** The event's content type. Could be <i>application/json</i> or <i>application/octet-stream</i>. */
  def getEventContentType: String

  /** The event's creation date. */
  def getEventCreateDate: Instant

  /** The event's unique identifier. */
  def getEventId: UUID

  /** The event's type. */
  def getEventType: String

  /** The stream's unique identifier. */
  def getStreamId: String

  /** The stream's revision. */
  def getStreamRevision: Long

  /** The event's user metadata. */
  def getUserMetadata: Array[Byte]

  /** The event's payload data if the event is a link event. */
  def getLinkEventData: Array[Byte]

  /** The event's content type if the event is a link event. */
  def getLinkEventContentType: String

  /** The event's creation date if the event is a link event. */
  def getLinkEventCreateDate: Instant

  /** The event's unique identifier if the event is a link event. */
  def getLinkEventId: UUID

  /** The event's type if the event is a link event. */
  def getLinkEventType: String

  /** The stream's unique identifier if the event is a link event. */
  def getLinkStreamId: String

  /** The stream's revision if the event is a link event. */
  def getLinkStreamRevision: Long

  /** The event's user metadata if the event is a link event. */
  def getLinkUserMetadata: Array[Byte]

  /** The transaction log position of the event. */
  def getPosition: Option[Position]

  def getResolvedEventUnsafe: ResolvedEvent
}

object Event {

  private[dolphin] def make(
    ctx: dbclient.ResolvedEvent
  ): Event =
    new Event {

      private def getRecordedEvent       = ctx.getOriginalEvent
      private def getLinkedRecordedEvent = ctx.getLink

      /** The event's payload data.
        */
      def getEventData: Array[Byte] = getRecordedEvent.getEventData

      /** The event's content type. Could be <i>application/json</i> or <i>application/octet-stream</i>.
        */
      def getEventContentType: String = getRecordedEvent.getContentType

      /** The event's creation date.
        */
      def getEventCreateDate: Instant = getRecordedEvent.getCreated

      /** The event's unique identifier.
        */
      def getEventId: UUID = getRecordedEvent.getEventId

      /** The event's type. */
      def getEventType: String = getRecordedEvent.getEventType

      /** The stream's unique identifier.
        */
      def getStreamId: String = getRecordedEvent.getStreamId

      /** The stream's revision.
        */
      def getStreamRevision: Long = getRecordedEvent.getRevision

      /** The event's user metadata.
        */
      def getUserMetadata: Array[Byte] = getRecordedEvent.getUserMetadata

      /** The event's payload data if the event is a link event.
        */
      def getLinkEventData: Array[Byte] = getLinkedRecordedEvent.getEventData

      /** The event's content type. Could be <i>application/json</i> or <i>application/octet-stream</i>.
        */
      def getLinkEventContentType: String = getLinkedRecordedEvent.getContentType

      /** The event's creation date if the event is a link event.
        */
      def getLinkEventCreateDate: Instant = getLinkedRecordedEvent.getCreated

      /** The event's unique identifier if the event is a link event.
        */
      def getLinkEventId: UUID = getLinkedRecordedEvent.getEventId

      /** The event's type if the event is a link event.
        */
      def getLinkEventType: String = getLinkedRecordedEvent.getEventType

      /** The stream's unique identifier if the event is a link event.
        */
      def getLinkStreamId: String = getLinkedRecordedEvent.getStreamId

      /** The stream's revision if the event is a link event.
        */
      def getLinkStreamRevision: Long = getLinkedRecordedEvent.getRevision

      /** The event's user metadata if the event is a link event.
        */
      def getLinkUserMetadata: Array[Byte] = getLinkedRecordedEvent.getUserMetadata

      /** The transaction log position of the event.
        */
      def getPosition: Option[Position] = ctx.getPosition.toScala.map(_.toScala)

      /** Gives access to the underlying [[ResolvedEvent]]. */
      def getResolvedEventUnsafe: ResolvedEvent = ctx

    }
}
