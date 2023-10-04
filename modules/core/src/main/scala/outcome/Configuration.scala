// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.outcome

import scala.concurrent.duration.FiniteDuration

import dolphin.concurrent.{ConsumerStrategy, Position}

import com.eventstore.dbclient

sealed trait Configuration {

  /** The amount of time to try to checkpoint after. */
  def getCheckpointAfter: FiniteDuration

  /** The amount of time in milliseconds to try to checkpoint after. */
  def getCheckpointAfterInMs: Int

  /** The minimum number of messages to process before a checkpoint may be written. */
  def getCheckpointLowerBound: Int

  /** The maximum number of messages not checkpointed before forcing a checkpoint. */
  def getCheckpointUpperBound: Int

  /** The number of events to cache when catching up. Default 500. */
  def getHistoryBufferSize: Int

  /** The size of the buffer (in-memory) listening to live messages as they happen before paging occurs. Default 500.
    */
  def getLiveBufferSize: Int

  /** The maximum number of retries (due to timeout) before a message is considered to be parked.
    */
  def getMaxRetryCount: Int

  /** The maximum number of subscribers allowed.
    */
  def getMaxSubscriberCount: Int

  /** The amount of time after which to consider a message as timed out and retried.
    */
  def getMessageTimeout: FiniteDuration

  /** The amount of time in milliseconds after which to consider a message as timed out and retried.
    */
  def getMessageTimeoutMs: Int

  /** The strategy to use for distributing events to client consumers.
    */
  def getNamedConsumerStrategy: ConsumerStrategy

  /** The number of events read at a time when catching up.
    */
  def getReadBatchSize: Int

  /** Whether to track latency statistics on this subscription.
    */
  def isExtraStatistics: Boolean

  /** If true, link resolution is enabled. The best way to explain link resolution is when using system projections.
    * When reading the stream <b>\$streams</b>, each event is actually a link pointing to the first event of a stream.
    * By enabling link resolution feature, EventStoreDB will also return the event targeted by the link.
    */
  def isResolveLinkTos: Boolean

  /** If true, link resolution is enabled. The best way to explain link resolution is when using system projections.
    * When reading the stream <b>\$streams</b>, each event is actually a link pointing to the first event of a stream.
    * By enabling link resolution feature, EventStoreDB will also return the event targeted by the link.
    */
  def shouldResolveLinkTos: Boolean
}

object Configuration {

  sealed trait ConfigurationWithStream extends Configuration {

    /** Checks if it's the beginning of the stream. */
    def isStart: Boolean

    /** Checks if it's the end of the stream. */
    def isEnd: Boolean

    /** Checks it's a specific position and returns the value. */
    def position: Option[Long]
  }

  sealed trait ConfigurationWithAll extends Configuration {

    /** Where to start subscription from. This can be from the start of the <b>\$all</b> stream, from the end of the
      * <b>\$all</b> stream at the time of creation, or from an inclusive position in <b>\$all</b> stream.
      */
    def getStartFromPosition: Option[Position]

    /** Checks if it's the beginning of the stream. */
    def isStart: Boolean

    /** Checks if it's the end of the stream. */
    def isEnd: Boolean
  }

  private[dolphin] def makeStream(
    ctx: dbclient.PersistentSubscriptionToStreamSettings
  ): ConfigurationWithStream =
    new ConfigurationWithStream {

      import dolphin.concurrent.ConsumerStrategy.*
      import dolphin.concurrent.ConsumerStrategy

      import scala.jdk.OptionConverters.*
      import scala.jdk.DurationConverters.*
      import scala.concurrent.duration.FiniteDuration

      def getCheckpointAfter: FiniteDuration = ctx.getCheckpointAfter.toScala

      def getCheckpointAfterInMs: Int = ctx.getCheckpointAfterInMs

      def getCheckpointLowerBound: Int = ctx.getCheckpointLowerBound

      def getCheckpointUpperBound: Int = ctx.getCheckpointUpperBound

      def getHistoryBufferSize: Int = ctx.getHistoryBufferSize

      def getLiveBufferSize: Int = ctx.getLiveBufferSize

      def getMaxRetryCount: Int = ctx.getMaxRetryCount

      def getMaxSubscriberCount: Int = ctx.getMaxSubscriberCount

      def getMessageTimeout: FiniteDuration = ctx.getMessageTimeout.toScala

      def getMessageTimeoutMs: Int = ctx.getMessageTimeoutMs

      def getNamedConsumerStrategy: ConsumerStrategy = ctx.getNamedConsumerStrategy.toScala

      def getReadBatchSize: Int = ctx.getReadBatchSize

      def isExtraStatistics: Boolean = ctx.isExtraStatistics

      def isResolveLinkTos: Boolean = ctx.isResolveLinkTos

      def shouldResolveLinkTos: Boolean = ctx.shouldResolveLinkTos

      /** Checks if it's the beginning of the stream. */
      def isStart: Boolean = ctx.getStartFrom.isStart

      /** Checks if it's the end of the stream. */
      def isEnd: Boolean = ctx.getStartFrom.isEnd

      /** Checks it's a specific position and returns the value. */
      def position: Option[Long] = ctx.getStartFrom.getPosition.toScala.map(_.longValue())

    }

  private[dolphin] def makeAll(
    ctx: dbclient.PersistentSubscriptionToAllSettings
  ): ConfigurationWithAll =
    new ConfigurationWithAll {

      import dolphin.concurrent.ConsumerStrategy.*
      import dolphin.concurrent.ConsumerStrategy
      import dolphin.concurrent.Position.*

      import scala.jdk.OptionConverters.*
      import scala.jdk.DurationConverters.*
      import scala.concurrent.duration.FiniteDuration

      /** Where to start subscription from. This can be from the start of the <b>\$all</b> stream, from the end of the
        * <b>\$all</b> stream at the time of creation, or from an inclusive position in <b>\$all</b> stream.
        */
      def getStartFromPosition: Option[Position] = ctx.getStartFrom.getPosition.toScala.map(_.toScala)

      /** Checks if it's the beginning of the stream. */
      def isStart: Boolean = ctx.getStartFrom.isStart

      /** Checks if it's the end of the stream. */
      def isEnd: Boolean = ctx.getStartFrom.isEnd

      /** The amount of time to try to checkpoint after. */
      def getCheckpointAfter: FiniteDuration = ctx.getCheckpointAfter.toScala

      /** The amount of time in milliseconds to try to checkpoint after. */
      def getCheckpointAfterInMs: Int = ctx.getCheckpointAfterInMs

      /** The minimum number of messages to process before a checkpoint may be written. */
      def getCheckpointLowerBound: Int = ctx.getCheckpointLowerBound

      /** The maximum number of messages not checkpointed before forcing a checkpoint. */
      def getCheckpointUpperBound: Int = ctx.getCheckpointUpperBound

      /** The number of events to cache when catching up. Default 500. */
      def getHistoryBufferSize: Int = ctx.getHistoryBufferSize

      /** The size of the buffer (in-memory) listening to live messages as they happen before paging occurs. Default
        * 500.
        */
      def getLiveBufferSize: Int = ctx.getLiveBufferSize

      /** The maximum number of retries (due to timeout) before a message is considered to be parked.
        */
      def getMaxRetryCount: Int = ctx.getMaxRetryCount

      /** The maximum number of subscribers allowed.
        */
      def getMaxSubscriberCount: Int = ctx.getMaxSubscriberCount

      /** The amount of time after which to consider a message as timed out and retried.
        */
      def getMessageTimeout: FiniteDuration = ctx.getMessageTimeout.toScala

      /** The amount of time in milliseconds after which to consider a message as timed out and retried.
        */
      def getMessageTimeoutMs: Int = ctx.getMessageTimeoutMs

      /** The strategy to use for distributing events to client consumers.
        */
      def getNamedConsumerStrategy: ConsumerStrategy = ctx.getNamedConsumerStrategy.toScala

      /** The number of events read at a time when catching up.
        */
      def getReadBatchSize: Int = ctx.getReadBatchSize

      /** Whether to track latency statistics on this subscription.
        */
      def isExtraStatistics: Boolean = ctx.isExtraStatistics

      /** If true, link resolution is enabled. The best way to explain link resolution is when using system projections.
        * When reading the stream <b>\$streams</b>, each event is actually a link pointing to the first event of a
        * stream. By enabling link resolution feature, EventStoreDB will also return the event targeted by the link.
        */
      def isResolveLinkTos: Boolean = ctx.isResolveLinkTos

      /** If true, link resolution is enabled. The best way to explain link resolution is when using system projections.
        * When reading the stream <b>\$streams</b>, each event is actually a link pointing to the first event of a
        * stream. By enabling link resolution feature, EventStoreDB will also return the event targeted by the link.
        */
      def shouldResolveLinkTos: Boolean = ctx.shouldResolveLinkTos
    }

}
