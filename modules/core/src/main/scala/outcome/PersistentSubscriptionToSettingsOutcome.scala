// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.outcome

import scala.concurrent.duration.FiniteDuration

import dolphin.concurrent.ConsumerStrategy

import cats.Applicative
import com.eventstore.dbclient

sealed trait PersistentSubscriptionToSettingsOutcome[F[_]] {

  /** The amount of time to try to checkpoint after. */
  def getCheckpointAfter: F[FiniteDuration]

  /** The amount of time in milliseconds to try to checkpoint after. */
  def getCheckpointAfterInMs: F[Int]

  /** The minimum number of messages to process before a checkpoint may be written. */
  def getCheckpointLowerBound: F[Int]

  /** The maximum number of messages not checkpointed before forcing a checkpoint. */
  def getCheckpointUpperBound: F[Int]

  /** The number of events to cache when catching up. Default 500. */
  def getHistoryBufferSize: F[Int]

  /** The size of the buffer (in-memory) listening to live messages as they happen before paging occurs. Default 500.
    */
  def getLiveBufferSize: F[Int]

  /** The maximum number of retries (due to timeout) before a message is considered to be parked.
    */
  def getMaxRetryCount: F[Int]

  /** The maximum number of subscribers allowed.
    */
  def getMaxSubscriberCount: F[Int]

  /** The amount of time after which to consider a message as timed out and retried.
    */
  def getMessageTimeout: F[FiniteDuration]

  /** The amount of time in milliseconds after which to consider a message as timed out and retried.
    */
  def getMessageTimeoutMs: F[Int]

  /** The strategy to use for distributing events to client consumers.
    */
  def getNamedConsumerStrategy: F[ConsumerStrategy]

  /** The number of events read at a time when catching up.
    */
  def getReadBatchSize: F[Int]

  /** Whether to track latency statistics on this subscription.
    */
  def isExtraStatistics: F[Boolean]

  /** If true, link resolution is enabled. The best way to explain link resolution is when using system projections.
    * When reading the stream <b>\$streams</b>, each event is actually a link pointing to the first event of a stream.
    * By enabling link resolution feature, EventStoreDB will also return the event targeted by the link.
    */
  def isResolveLinkTos: F[Boolean]

  /** If true, link resolution is enabled. The best way to explain link resolution is when using system projections.
    * When reading the stream <b>\$streams</b>, each event is actually a link pointing to the first event of a stream.
    * By enabling link resolution feature, EventStoreDB will also return the event targeted by the link.
    */
  def shouldResolveLinkTos: F[Boolean]
}

object PersistentSubscriptionToSettingsOutcome {

  private[dolphin] def makeStream[F[_]: Applicative](
    result: dbclient.PersistentSubscriptionToStreamSettings
  ) =
    new PersistentSubscriptionToSettingsOutcome[F] {

      import dolphin.concurrent.ConsumerStrategy.*
      import dolphin.concurrent.ConsumerStrategy

      import scala.jdk.OptionConverters.*
      import scala.jdk.DurationConverters.*
      import scala.concurrent.duration.FiniteDuration

      def getCheckpointAfter: F[FiniteDuration] = Applicative[F].pure(result.getCheckpointAfter.toScala)

      def getCheckpointAfterInMs: F[Int] = Applicative[F].pure(result.getCheckpointAfterInMs)

      def getCheckpointLowerBound: F[Int] = Applicative[F].pure(result.getCheckpointLowerBound)

      def getCheckpointUpperBound: F[Int] = Applicative[F].pure(result.getCheckpointUpperBound)

      def getHistoryBufferSize: F[Int] = Applicative[F].pure(result.getHistoryBufferSize)

      def getLiveBufferSize: F[Int] = Applicative[F].pure(result.getLiveBufferSize)

      def getMaxRetryCount: F[Int] = Applicative[F].pure(result.getMaxRetryCount)

      def getMaxSubscriberCount: F[Int] = Applicative[F].pure(result.getMaxSubscriberCount)

      def getMessageTimeout: F[FiniteDuration] = Applicative[F].pure(result.getMessageTimeout.toScala)

      def getMessageTimeoutMs: F[Int] = Applicative[F].pure(result.getMessageTimeoutMs)

      def getNamedConsumerStrategy: F[ConsumerStrategy] = Applicative[F].pure(
        result.getNamedConsumerStrategy.toScala
      )

      def getReadBatchSize: F[Int] = Applicative[F].pure(result.getReadBatchSize)

      def isExtraStatistics: F[Boolean] = Applicative[F].pure(result.isExtraStatistics)

      def isResolveLinkTos: F[Boolean] = Applicative[F].pure(result.isResolveLinkTos)

      def shouldResolveLinkTos: F[Boolean] = Applicative[F].pure(result.shouldResolveLinkTos)

      /** Checks if it's the beginning of the stream. */
      def isStart: F[Boolean] = Applicative[F].pure(result.getStartFrom.isStart)

      /** Checks if it's the end of the stream. */
      def isEnd: F[Boolean] = Applicative[F].pure(result.getStartFrom.isEnd)

      /** Checks it's a specific position and returns the value. */
      def position: F[Option[Long]] = Applicative[F].pure(result.getStartFrom.getPosition.toScala.map(_.longValue()))
    }

  private[dolphin] def makeAll[F[_]: Applicative](
    result: dbclient.PersistentSubscriptionToAllSettings
  ) =
    new PersistentSubscriptionToSettingsOutcome[F] {
      import dolphin.concurrent.Position

      import dolphin.concurrent.ConsumerStrategy.*
      import dolphin.concurrent.ConsumerStrategy
      import dolphin.concurrent.Position.*

      import scala.jdk.OptionConverters.*
      import scala.jdk.DurationConverters.*
      import scala.concurrent.duration.FiniteDuration

      /** Where to start subscription from. This can be from the start of the <b>\$all</b> stream, from the end of the
        * <b>\$all</b> stream at the time of creation, or from an inclusive position in <b>\$all</b> stream.
        */
      def getStartFromPosition: F[Option[Position]] = Applicative[F].pure(
        result.getStartFrom.getPosition.toScala.map(_.toScala)
      )

      /** Checks if it's the beginning of the stream. */
      def isStart: F[Boolean] = Applicative[F].pure(result.getStartFrom.isStart)

      /** Checks if it's the end of the stream. */
      def isEnd: F[Boolean] = Applicative[F].pure(result.getStartFrom.isEnd)

      /** The amount of time to try to checkpoint after. */
      def getCheckpointAfter: F[FiniteDuration] = Applicative[F].pure(result.getCheckpointAfter.toScala)

      /** The amount of time in milliseconds to try to checkpoint after. */
      def getCheckpointAfterInMs: F[Int] = Applicative[F].pure(result.getCheckpointAfterInMs)

      /** The minimum number of messages to process before a checkpoint may be written. */
      def getCheckpointLowerBound: F[Int] = Applicative[F].pure(result.getCheckpointLowerBound)

      /** The maximum number of messages not checkpointed before forcing a checkpoint. */
      def getCheckpointUpperBound: F[Int] = Applicative[F].pure(result.getCheckpointUpperBound)

      /** The number of events to cache when catching up. Default 500. */
      def getHistoryBufferSize: F[Int] = Applicative[F].pure(result.getHistoryBufferSize)

      /** The size of the buffer (in-memory) listening to live messages as they happen before paging occurs. Default
        * 500.
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
      def getMessageTimeout: F[FiniteDuration] = Applicative[F].pure(result.getMessageTimeout.toScala)

      /** The amount of time in milliseconds after which to consider a message as timed out and retried.
        */
      def getMessageTimeoutMs: F[Int] = Applicative[F].pure(result.getMessageTimeoutMs)

      /** The strategy to use for distributing events to client consumers.
        */
      def getNamedConsumerStrategy: F[ConsumerStrategy] = Applicative[F].pure(result.getNamedConsumerStrategy.toScala)

      /** The number of events read at a time when catching up.
        */
      def getReadBatchSize: F[Int] = Applicative[F].pure(result.getReadBatchSize)

      /** Whether to track latency statistics on this subscription.
        */
      def isExtraStatistics: F[Boolean] = Applicative[F].pure(result.isExtraStatistics)

      /** If true, link resolution is enabled. The best way to explain link resolution is when using system projections.
        * When reading the stream <b>\$streams</b>, each event is actually a link pointing to the first event of a
        * stream. By enabling link resolution feature, EventStoreDB will also return the event targeted by the link.
        */
      def isResolveLinkTos: F[Boolean] = Applicative[F].pure(result.isResolveLinkTos)

      /** If true, link resolution is enabled. The best way to explain link resolution is when using system projections.
        * When reading the stream <b>\$streams</b>, each event is actually a link pointing to the first event of a
        * stream. By enabling link resolution feature, EventStoreDB will also return the event targeted by the link.
        */
      def shouldResolveLinkTos: F[Boolean] = Applicative[F].pure(result.shouldResolveLinkTos)
    }

}
