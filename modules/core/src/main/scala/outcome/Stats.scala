// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.outcome

import dolphin.concurrent.Position
import dolphin.concurrent.Position.*

import com.eventstore.dbclient

sealed trait Stats {

  /** Average number of events per seconds. */
  def getAveragePerSecond: Int

  /** Number of events seen since last measurement on this connection. */
  def getCountSinceLastMeasurement: Int

  /** Number of events in the live buffer. */
  def getLiveBufferCount: Long

  /** Current number of outstanding messages. */
  def getOutstandingMessagesCount: Int

  /** The current number of parked messages. */
  def getParkedMessageCount: Long

  /** Number of events in the read buffer. */
  def getReadBufferCount: Int

  /** Number of events in the retry buffer. */
  def getRetryBufferCount: Int

  /** Current in flight messages across the persistent subscription group. */
  def getTotalInFlightMessages: Int

  /** Total number of events processed by subscription. */
  def getTotalItemsProcessed: Long
}

object Stats {

  sealed trait StatsWithStream extends Stats {
    def getLastCheckpointedEventRevision: Option[Long]
    def getLastKnownEventRevision: Option[Long]
  }

  sealed trait StatsWithAll extends Stats {

    /** The transaction log position of the last checkpoint. */
    def getLastCheckpointedEventPosition: Option[Position]

    /** The transaction log position of the last known event. */
    def getLastKnownEventPosition: Option[Position]
  }

  private[dolphin] def makeAll(
    ctx: dbclient.PersistentSubscriptionToAllStats
  ): StatsWithAll =
    new StatsWithAll {

      import scala.jdk.OptionConverters.*

      /** The transaction log position of the last checkpoint. */
      def getLastCheckpointedEventPosition
        : Option[Position] = ctx.getLastCheckpointedEventPosition.toScala.map(_.toScala)

      /** The transaction log position of the last known event. */
      def getLastKnownEventPosition: Option[Position] = ctx.getLastKnownEventPosition.toScala.map(_.toScala)

      /** Average number of events per seconds. */
      def getAveragePerSecond: Int = ctx.getAveragePerSecond

      /** Number of events seen since last measurement on this connection. */
      def getCountSinceLastMeasurement: Int = ctx.getCountSinceLastMeasurement

      /** Number of events in the live buffer. */
      def getLiveBufferCount: Long = ctx.getLiveBufferCount

      /** Current number of outstanding messages. */
      def getOutstandingMessagesCount: Int = ctx.getOutstandingMessagesCount

      /** The current number of parked messages. */
      def getParkedMessageCount: Long = ctx.getParkedMessageCount

      /** Number of events in the read buffer. */
      def getReadBufferCount: Int = ctx.getReadBufferCount

      /** Number of events in the retry buffer. */
      def getRetryBufferCount: Int = ctx.getRetryBufferCount

      /** Current in flight messages across the persistent subscription group. */
      def getTotalInFlightMessages: Int = ctx.getTotalInFlightMessages

      /** Total number of events processed by subscription. */
      def getTotalItemsProcessed: Long = ctx.getTotalItems
    }

  private[dolphin] def makeStream(
    ctx: dbclient.PersistentSubscriptionToStreamStats
  ): StatsWithStream =
    new StatsWithStream {
      import scala.jdk.OptionConverters.*

      /** The revision number of the last checkpoint. */
      def getLastCheckpointedEventRevision
        : Option[Long] = ctx.getLastCheckpointedEventRevision.toScala.map(_.longValue())

      /** The revision number of the last known event. */
      def getLastKnownEventRevision: Option[Long] = ctx.getLastKnownEventRevision.toScala.map(_.longValue())

      /** Average number of events per seconds. */
      def getAveragePerSecond: Int = ctx.getAveragePerSecond

      /** Number of events seen since last measurement on this connection. */
      def getCountSinceLastMeasurement: Int = ctx.getCountSinceLastMeasurement

      /** Number of events in the live buffer. */
      def getLiveBufferCount: Long = ctx.getLiveBufferCount

      /** Current number of outstanding messages. */
      def getOutstandingMessagesCount: Int = ctx.getOutstandingMessagesCount

      /** The current number of parked messages. */
      def getParkedMessageCount: Long = ctx.getParkedMessageCount

      /** Number of events in the read buffer. */
      def getReadBufferCount: Int = ctx.getReadBufferCount

      /** Number of events in the retry buffer. */
      def getRetryBufferCount: Int = ctx.getRetryBufferCount

      /** Current in flight messages across the persistent subscription group. */
      def getTotalInFlightMessages: Int = ctx.getTotalInFlightMessages

      /** Total number of events processed by subscription. */
      def getTotalItemsProcessed: Long = ctx.getTotalItems

    }
}
