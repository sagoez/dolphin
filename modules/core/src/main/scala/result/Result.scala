// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.result

import java.time.Duration

import scala.jdk.OptionConverters.*

import dolphin.concurrent.ConsumerStrategy.*
import dolphin.concurrent.{ConsumerStrategy, NackAction}

import cats.Applicative
import com.eventstore.dbclient

sealed trait Result

object Result {

  final case class DeleteResult[F[_]: Applicative](
    private val result: dbclient.DeleteResult
  ) extends Result {

    /** Returns the commit position.
      */
    def getCommitUnsigned: F[Long] = Applicative[F].pure(result.getPosition.getCommitUnsigned)

    /** Returns the prepare position.
      */
    def getPrepareUnsigned: F[Long] = Applicative[F].pure(result.getPosition.getPrepareUnsigned)
  }

  final case class ReadResult[F[_]: Applicative](
    private val result: dbclient.ReadResult
  ) extends Result {

    import java.time.Instant
    import java.util.UUID

    import scala.jdk.CollectionConverters.*

    import com.eventstore.dbclient.*
    import fs2.Stream

    /** Returns all the events of the read operation.
      */
    def getResolvedEvents: Stream[F, ResolvedEvent] = Stream(result.getEvents.asScala.toSeq*)

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
    def getFirstStreamPosition: F[Long] = Applicative[F].pure(result.getFirstStreamPosition)

    /** When reading from a regular stream, returns the last event revision number of the stream.
      */
    def getLastStreamPosition: F[Long] = Applicative[F].pure(result.getLastStreamPosition)

    /** When reading from <b>all</b> stream, returns the last event position.
      * @return
      *   None if reading from a regular stream.
      */
    def getLastAllStreamPosition: F[Option[Position]] = Applicative[F].pure(
      Option(result.getLastAllStreamPosition)
    )
  }

  final case class WriteResult[F[_]: Applicative](
    private val result: dbclient.WriteResult
  ) extends Result {

    import dolphin.concurrent.ExpectedRevision
    import dolphin.concurrent.ExpectedRevision.DbClientExpectedRevisionOps

    /** Returns the commit position.
      */
    def getCommitUnsigned: F[Long] = Applicative[F].pure(result.getLogPosition.getCommitUnsigned)

    /** Returns the prepare position.
      */
    def getPrepareUnsigned: F[Long] = Applicative[F].pure(result.getLogPosition.getPrepareUnsigned)

    /** Next expected version of the stream. Maps java to scala using reflection, in case of failure getting expected
      * revision of long type, it will throw an exception.
      */
    def getNextExpectedRevision: F[ExpectedRevision] = Applicative[F].pure(
      result.getNextExpectedRevision.fromJava
    )
  }

  final case class PersistentSubscriptionToAllInfoResult[F[_]: Applicative](
    private val result: dbclient.PersistentSubscriptionToAllInfo
  ) extends Result {

    /** Returns the commit position from the runtime persistent subscription statistics.
      */
    def getCommitUnsignedRuntime: F[Option[Long]] = Applicative[F].pure(
      result.getStats.getLastKnownEventPosition.toScala.map(_.getCommitUnsigned)
    )

    /** Returns the prepare position from the runtime persistent subscription statistics:
      */
    def getPrepareUnsignedRuntime: F[Option[Long]] = Applicative[F].pure(
      result.getStats.getLastKnownEventPosition.toScala.map(_.getPrepareUnsigned)
    )

    /** Returns the commit position from the settings used to create the persistent subscription:
      */
    def getCommitUnsignedSetting: F[Option[Long]] = Applicative[F].pure(
      result.getSettings.getStartFrom.getPosition.toScala.map(_.getCommitUnsigned)
    )

    /** Returns the prepare position from the settings used to create the persistent subscription:
      */
    def getPrepareUnsignedSetting: F[Option[Long]] = Applicative[F].pure(
      result.getSettings.getStartFrom.getPosition.toScala.map(_.getPrepareUnsigned)
    )

    /** Checks if it's the beginning of the stream. */
    def isStart: F[Boolean] = Applicative[F].pure(result.getSettings.getStartFrom.isStart)

    /** Checks if it's the end of the stream. */
    def isEnd: F[Boolean] = Applicative[F].pure(result.getSettings.getStartFrom.isEnd)

    /** The amount of time to try to checkpoint after. */
    def getCheckpointAfter: F[Duration] = Applicative[F].pure(result.getSettings.getCheckpointAfter)

    /** The amount of time in milliseconds to try to checkpoint after. */
    def getCheckpointAfterInMs: F[Int] = Applicative[F].pure(result.getSettings.getCheckpointAfterInMs)

    /** The minimum number of messages to process before a checkpoint may be written. */
    def getCheckpointLowerBound: F[Int] = Applicative[F].pure(result.getSettings.getCheckpointLowerBound)

    /** The maximum number of messages not checkpointed before forcing a checkpoint. */
    def getCheckpointUpperBound: F[Int] = Applicative[F].pure(result.getSettings.getCheckpointUpperBound)

    /** The number of events to cache when catching up. Default 500. */
    def getHistoryBufferSize: F[Int] = Applicative[F].pure(result.getSettings.getHistoryBufferSize)

    /** The size of the buffer (in-memory) listening to live messages as they happen before paging occurs. Default 500.
      */
    def getLiveBufferSize: F[Int] = Applicative[F].pure(result.getSettings.getLiveBufferSize)

    /** The maximum number of retries (due to timeout) before a message is considered to be parked.
      */
    def getMaxRetryCount: F[Int] = Applicative[F].pure(result.getSettings.getMaxRetryCount)

    /** The maximum number of subscribers allowed.
      */
    def getMaxSubscriberCount: F[Int] = Applicative[F].pure(result.getSettings.getMaxSubscriberCount)

    /** The amount of time after which to consider a message as timed out and retried.
      */
    def getMessageTimeout: F[Duration] = Applicative[F].pure(result.getSettings.getMessageTimeout)

    /** The amount of time in milliseconds after which to consider a message as timed out and retried.
      */
    def getMessageTimeoutMs: F[Int] = Applicative[F].pure(result.getSettings.getMessageTimeoutMs)

    /** The strategy to use for distributing events to client consumers.
      */
    def getNamedConsumerStrategy: F[ConsumerStrategy] = Applicative[F].pure(
      result.getSettings.getNamedConsumerStrategy.fromJava
    )

    /** The number of events read at a time when catching up.
      */
    def getReadBatchSize: F[Int] = Applicative[F].pure(result.getSettings.getReadBatchSize)

    /** Whether to track latency statistics on this subscription.
      */
    def isExtraStatistics: F[Boolean] = Applicative[F].pure(result.getSettings.isExtraStatistics)

    /** If true, link resolution is enabled. The best way to explain link resolution is when using system projections.
      * When reading the stream <b>\$streams</b>, each event is actually a link pointing to the first event of a stream.
      * By enabling link resolution feature, EventStoreDB will also return the event targeted by the link.
      */
    def isResolveLinkTos: F[Boolean] = Applicative[F].pure(result.getSettings.isResolveLinkTos)

    /** If true, link resolution is enabled. The best way to explain link resolution is when using system projections.
      * When reading the stream <b>\$streams</b>, each event is actually a link pointing to the first event of a stream.
      * By enabling link resolution feature, EventStoreDB will also return the event targeted by the link.
      */
    def shouldResolveLinkTos: F[Boolean] = Applicative[F].pure(result.getSettings.shouldResolveLinkTos)
  }

  final case class PersistentSubscriptionToStreamInfoResult[F[_]: Applicative](
    private val result: dbclient.PersistentSubscriptionToStreamInfo
  ) extends Result {

    /** The revision number of the last known event. */
    def getLastKnownEventRevision: F[Option[Long]] = Applicative[F].pure(
      result.getStats.getLastKnownEventRevision.toScala.map(_.longValue())
    )

    /** The revision number of the last known event. */
    def getLastKnownEventNumber: F[Option[Long]] = Applicative[F].pure(
      result.getStats.getLastCheckpointedEventRevision.toScala.map(_.longValue())
    )

    /** Where to start subscription from. This can be from the start of the <b>\$all</b> stream, from the end of the
      * <b>\$all</b> stream at the time of creation, or from an inclusive position in <b>\$all</b> stream.
      */
    def getStartFrom: F[Option[Long]] = Applicative[F].pure(
      result.getSettings.getStartFrom.getPosition.toScala.map(_.longValue())
    )

    /** Checks if it's the beginning of the stream. */
    def isStart: F[Boolean] = Applicative[F].pure(result.getSettings.getStartFrom.isStart)

    /** Checks if it's the end of the stream. */
    def isEnd: F[Boolean] = Applicative[F].pure(result.getSettings.getStartFrom.isEnd)

    /** The amount of time to try to checkpoint after. */
    def getCheckpointAfter: F[Duration] = Applicative[F].pure(result.getSettings.getCheckpointAfter)

    /** The amount of time in milliseconds to try to checkpoint after. */
    def getCheckpointAfterInMs: F[Int] = Applicative[F].pure(result.getSettings.getCheckpointAfterInMs)

    /** The minimum number of messages to process before a checkpoint may be written. */
    def getCheckpointLowerBound: F[Int] = Applicative[F].pure(result.getSettings.getCheckpointLowerBound)

    /** The maximum number of messages not checkpointed before forcing a checkpoint. */
    def getCheckpointUpperBound: F[Int] = Applicative[F].pure(result.getSettings.getCheckpointUpperBound)

    /** The number of events to cache when catching up. Default 500. */
    def getHistoryBufferSize: F[Int] = Applicative[F].pure(result.getSettings.getHistoryBufferSize)

    /** The size of the buffer (in-memory) listening to live messages as they happen before paging occurs. Default 500.
      */
    def getLiveBufferSize: F[Int] = Applicative[F].pure(result.getSettings.getLiveBufferSize)

    /** The maximum number of retries (due to timeout) before a message is considered to be parked.
      */
    def getMaxRetryCount: F[Int] = Applicative[F].pure(result.getSettings.getMaxRetryCount)

    /** The maximum number of subscribers allowed.
      */
    def getMaxSubscriberCount: F[Int] = Applicative[F].pure(result.getSettings.getMaxSubscriberCount)

    /** The amount of time after which to consider a message as timed out and retried.
      */
    def getMessageTimeout: F[Duration] = Applicative[F].pure(result.getSettings.getMessageTimeout)

    /** The amount of time in milliseconds after which to consider a message as timed out and retried.
      */
    def getMessageTimeoutMs: F[Int] = Applicative[F].pure(result.getSettings.getMessageTimeoutMs)

    /** The strategy to use for distributing events to client consumers.
      */
    def getNamedConsumerStrategy: F[ConsumerStrategy] = Applicative[F].pure(
      result.getSettings.getNamedConsumerStrategy.fromJava
    )

    /** The number of events read at a time when catching up.
      */
    def getReadBatchSize: F[Int] = Applicative[F].pure(result.getSettings.getReadBatchSize)

    /** Whether to track latency statistics on this subscription.
      */
    def isExtraStatistics: F[Boolean] = Applicative[F].pure(result.getSettings.isExtraStatistics)

    /** If true, link resolution is enabled. The best way to explain link resolution is when using system projections.
      * When reading the stream <b>\$streams</b>, each event is actually a link pointing to the first event of a stream.
      * By enabling link resolution feature, EventStoreDB will also return the event targeted by the link.
      */
    def isResolveLinkTos: F[Boolean] = Applicative[F].pure(result.getSettings.isResolveLinkTos)

    /** If true, link resolution is enabled. The best way to explain link resolution is when using system projections.
      * When reading the stream <b>\$streams</b>, each event is actually a link pointing to the first event of a stream.
      * By enabling link resolution feature, EventStoreDB will also return the event targeted by the link.
      */
    def shouldResolveLinkTos: F[Boolean] = Applicative[F].pure(result.getSettings.shouldResolveLinkTos)

  }

  final case class PersistentSubscriptionInfoResult[F[_]: Applicative](
    private val result: dbclient.PersistentSubscriptionInfo
  ) extends Result {
    import scala.jdk.CollectionConverters.*

    /** The source of events for the subscription. */
    def getEventSource: F[String] = Applicative[F].pure(result.getEventSource)

    /** The group name given on creation. */
    def getGroupName: F[String] = Applicative[F].pure(result.getGroupName)

    /** The current status of the subscription. */
    def getStatus: F[String] = Applicative[F].pure(result.getStatus)

    /** Active connections to the subscription. */
    def getConnections: F[List[PersistentSubscriptionConnectionInfoResult[F]]] = Applicative[F].pure(
      result
        .getConnections
        .asScala
        .toList
        .map(PersistentSubscriptionConnectionInfoResult(_))
    )

  }

  final case class PersistentSubscriptionConnectionInfoResult[F[_]: Applicative](
    private val result: dbclient.PersistentSubscriptionConnectionInfo
  ) extends Result {

    import scala.jdk.CollectionConverters.*

    /** Number of available slots. */
    def getAvailableSlots: F[Long] = Applicative[F].pure(result.getAvailableSlots)

    /** Average events per second on this connection. */
    def getAverageItemsPerSecond: F[Double] = Applicative[F].pure(result.getAverageItemsPerSecond)

    /** Connection name. */
    def getConnectionName: F[String] = Applicative[F].pure(result.getConnectionName)

    /** Number of items seen since last measurement on this connection. */
    def getCountSinceLastMeasurement: F[Long] = Applicative[F].pure(result.getCountSinceLastMeasurement)

    /** Timing measurements for the connection. */
    def getExtraStatistics: F[Map[String, Long]] = Applicative[F].pure(
      result.getExtraStatistics.asScala.map { case (key, value) => (key, value.longValue()) }.toMap
    )

    /** Origin of this connection. */
    def getFrom: F[String] = Applicative[F].pure(result.getFrom)

    /** Number of in flight messages on this connection. */
    def getInFlightMessages: F[Long] = Applicative[F].pure(result.getInFlightMessages)

    /** Total items on this connection. */
    def getTotalItems: F[Long] = Applicative[F].pure(result.getTotalItems)

    /** Connection's username. */
    def getUsername: F[String] = Applicative[F].pure(result.getUsername)

  }

  // ack and nack should be used under the onEvent callback only, figure out how to enforce this
  final case class PersistentSubscriptionResult[F[_]: Applicative](
    private val result: dbclient.PersistentSubscription
  ) extends Result {

    import scala.jdk.CollectionConverters.*

    /** Acknowledges events have been successfully processed. */
    def ack(event: dbclient.ResolvedEvent): F[Unit] = Applicative[F].pure(
      result.ack(event)
    )

    /** Acknowledges events have been successfully processed. */
    def ack(events: List[dbclient.ResolvedEvent]): F[Unit] = Applicative[F].pure(
      result.ack(events.asJava.iterator())
    )

    /** Returns the persistent subscription's id. */
    def getSubscriptionId: F[String] = Applicative[F].pure(result.getSubscriptionId)

    /** Acknowledges events failed processing. */
    def nack(action: NackAction, reason: String, event: dbclient.ResolvedEvent): F[Unit] = Applicative[F].pure(
      result.nack(action.toJava, reason, event)
    )

    /** Acknowledges events failed processing. */
    def nack(action: NackAction, reason: String, events: List[dbclient.ResolvedEvent]): F[Unit] = Applicative[F].pure(
      result.nack(action.toJava, reason, events.asJava.iterator())
    )

    /** Stops the persistent subscription. */
    def stop: F[Unit] = Applicative[F].pure(result.stop())
  }

  final case class PersistentSubscriptionToStreamResult[F[_]: Applicative](
    private val result: dbclient.PersistentSubscriptionToStreamSettings
  ) extends Result {

    /** Checks if it's the beginning of the stream. */
    def isStart: F[Boolean] = Applicative[F].pure(result.getStartFrom.isStart)

    /** Checks if it's the end of the stream. */
    def isEnd: F[Boolean] = Applicative[F].pure(result.getStartFrom.isEnd)

    /** Checks it's a specific position and returns the value. */
    def position: F[Option[Long]] = Applicative[F].pure(result.getStartFrom.getPosition.toScala.map(_.longValue()))

    /** The amount of time to try to checkpoint after. */
    def getCheckpointAfter: F[Duration] = Applicative[F].pure(result.getCheckpointAfter)

    /** The amount of time in milliseconds to try to checkpoint after. */
    def getCheckpointAfterMillis: F[Int] = Applicative[F].pure(result.getCheckpointAfterInMs)

    /** Whether to track latency statistics on this subscription. */
    def isExtraStatistics: F[Boolean] = Applicative[F].pure(result.isExtraStatistics)

    /** If true, link resolution is enabled. The best way to explain link resolution is when using system projections.
      * When reading the stream <b>\$streams</b>, each event is actually a link pointing to the first event of a stream.
      * By enabling link resolution feature, EventStoreDB will also return the event targeted by the link.
      */
    def isResolveLinkTos: F[Boolean] = Applicative[F].pure(result.isResolveLinkTos)

    /** If true, link resolution is enabled. The best way to explain link resolution is when using system projections.
      * When reading the stream <b>\$streams</b>, each event is actually a link pointing to the first event of a stream.
      * By enabling link resolution feature, EventStoreDB will also return the event targeted by the link.
      */
    def shouldResolveLinkTos: F[Boolean] = Applicative[F].pure(result.shouldResolveLinkTos)

    /** The minimum number of messages to process before a checkpoint may be written. */
    def getCheckpointLowerBound: F[Int] = Applicative[F].pure(result.getCheckpointLowerBound)

    /** The maximum number of messages not checkpointed before forcing a checkpoint. */
    def getCheckpointUpperBound: F[Int] = Applicative[F].pure(result.getCheckpointUpperBound)

    /** The number of events to cache when catching up. Default 500. */
    def getHistoryBufferSize: F[Int] = Applicative[F].pure(result.getHistoryBufferSize)

    /** The size of the buffer (in-memory) listening to live messages as they happen before paging occurs. Default 500.
      */
    def getLiveBufferSize: F[Int] = Applicative[F].pure(result.getLiveBufferSize)

    /** The maximum number of retries (due to timeout) before a message is considered to be parked.
      */
    def getMaxRetryCount: F[Int] = Applicative[F].pure(result.getMaxRetryCount)

    /** The maximum number of subscribers allowed.
      */
    def getMaxSubscriberCount: F[Int] = Applicative[F].pure(result.getMaxSubscriberCount)

    /** The amount of time after which to consider a message as timed out and retried.
      */
    def getMessageTimeout: F[Duration] = Applicative[F].pure(result.getMessageTimeout)

    /** The amount of time in milliseconds after which to consider a message as timed out and retried.
      */
    def getMessageTimeoutMs: F[Int] = Applicative[F].pure(result.getMessageTimeoutMs)

    /** The strategy to use for distributing events to client consumers.
      */
    def getNamedConsumerStrategy: F[ConsumerStrategy] = Applicative[F].pure(
      result.getNamedConsumerStrategy.fromJava
    )

    /** The number of events read at a time when catching up.
      */
    def getReadBatchSize: F[Int] = Applicative[F].pure(result.getReadBatchSize)

  }

}
