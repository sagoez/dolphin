// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.outcome

import dolphin.concurrent.Position
import dolphin.concurrent.Position.*

import cats.Applicative
import com.eventstore.dbclient

sealed trait PersistentSubscriptionToStatsOutcome[F[_]] {

  /** Average number of events per seconds. */
  def getAveragePerSecond: F[Int]

  /** Number of events seen since last measurement on this connection. */
  def getCountSinceLastMeasurement: F[Int]

  /** Number of events in the live buffer. */
  def getLiveBufferCount: F[Long]

  /** Current number of outstanding messages. */
  def getOutstandingMessagesCount: F[Int]

  /** The current number of parked messages. */
  def getParkedMessageCount: F[Long]

  /** Number of events in the read buffer. */
  def getReadBufferCount: F[Int]

  /** Number of events in the retry buffer. */
  def getRetryBufferCount: F[Int]

  /** Current in flight messages across the persistent subscription group. */
  def getTotalInFlightMessages: F[Int]

  /** Total number of events processed by subscription. */
  def getTotalItemsProcessed: F[Long]
}

object PersistentSubscriptionToStatsOutcome {

  sealed trait PersistentSubscriptionToStatsWithStreamOutcome[F[_]] extends PersistentSubscriptionToStatsOutcome[F] {
    def getLastCheckpointedEventRevision: F[Option[Long]]
    def getLastKnownEventRevision: F[Option[Long]]
  }

  sealed trait PersistentSubscriptionToStatsWithAllOutcome[F[_]] extends PersistentSubscriptionToStatsOutcome[F] {

    /** The transaction log position of the last checkpoint. */
    def getLastCheckpointedEventPosition: F[Option[Position]]

    /** The transaction log position of the last known event. */
    def getLastKnownEventPosition: F[Option[Position]]
  }

  private[dolphin] def makeAll[F[_]: Applicative](
    ctx: dbclient.PersistentSubscriptionToAllStats
  ): PersistentSubscriptionToStatsWithAllOutcome[F] =
    new PersistentSubscriptionToStatsWithAllOutcome[F] {

      import scala.jdk.OptionConverters.*

      /** The transaction log position of the last checkpoint. */
      def getLastCheckpointedEventPosition: F[Option[Position]] = Applicative[F].pure(
        ctx.getLastCheckpointedEventPosition.toScala.map(_.toScala)
      )

      /** The transaction log position of the last known event. */
      def getLastKnownEventPosition: F[Option[Position]] = Applicative[F].pure(
        ctx.getLastKnownEventPosition.toScala.map(_.toScala)
      )

      /** Average number of events per seconds. */
      def getAveragePerSecond: F[Int] = Applicative[F].pure(ctx.getAveragePerSecond)

      /** Number of events seen since last measurement on this connection. */
      def getCountSinceLastMeasurement: F[Int] = Applicative[F].pure(ctx.getCountSinceLastMeasurement)

      /** Number of events in the live buffer. */
      def getLiveBufferCount: F[Long] = Applicative[F].pure(ctx.getLiveBufferCount)

      /** Current number of outstanding messages. */
      def getOutstandingMessagesCount: F[Int] = Applicative[F].pure(ctx.getOutstandingMessagesCount)

      /** The current number of parked messages. */
      def getParkedMessageCount: F[Long] = Applicative[F].pure(ctx.getParkedMessageCount)

      /** Number of events in the read buffer. */
      def getReadBufferCount: F[Int] = Applicative[F].pure(ctx.getReadBufferCount)

      /** Number of events in the retry buffer. */
      def getRetryBufferCount: F[Int] = Applicative[F].pure(ctx.getRetryBufferCount)

      /** Current in flight messages across the persistent subscription group. */
      def getTotalInFlightMessages: F[Int] = Applicative[F].pure(ctx.getTotalInFlightMessages)

      /** Total number of events processed by subscription. */
      def getTotalItemsProcessed: F[Long] = Applicative[F].pure(ctx.getTotalItems)
    }

  private[dolphin] def makeStream[F[_]: Applicative](
    ctx: dbclient.PersistentSubscriptionToStreamStats
  ): PersistentSubscriptionToStatsWithStreamOutcome[F] =
    new PersistentSubscriptionToStatsWithStreamOutcome[F] {
      import scala.jdk.OptionConverters.*

      /** The revision number of the last checkpoint. */
      def getLastCheckpointedEventRevision: F[Option[Long]] = Applicative[F].pure(
        ctx.getLastCheckpointedEventRevision.toScala.map(_.longValue())
      )

      /** The revision number of the last known event. */
      def getLastKnownEventRevision: F[Option[Long]] = Applicative[F].pure(
        ctx.getLastKnownEventRevision.toScala.map(_.longValue())
      )

      /** Average number of events per seconds. */
      def getAveragePerSecond: F[Int] = Applicative[F].pure(ctx.getAveragePerSecond)

      /** Number of events seen since last measurement on this connection. */
      def getCountSinceLastMeasurement: F[Int] = Applicative[F].pure(ctx.getCountSinceLastMeasurement)

      /** Number of events in the live buffer. */
      def getLiveBufferCount: F[Long] = Applicative[F].pure(ctx.getLiveBufferCount)

      /** Current number of outstanding messages. */
      def getOutstandingMessagesCount: F[Int] = Applicative[F].pure(ctx.getOutstandingMessagesCount)

      /** The current number of parked messages. */
      def getParkedMessageCount: F[Long] = Applicative[F].pure(ctx.getParkedMessageCount)

      /** Number of events in the read buffer. */
      def getReadBufferCount: F[Int] = Applicative[F].pure(ctx.getReadBufferCount)

      /** Number of events in the retry buffer. */
      def getRetryBufferCount: F[Int] = Applicative[F].pure(ctx.getRetryBufferCount)

      /** Current in flight messages across the persistent subscription group. */
      def getTotalInFlightMessages: F[Int] = Applicative[F].pure(ctx.getTotalInFlightMessages)

      /** Total number of events processed by subscription. */
      def getTotalItemsProcessed: F[Long] = Applicative[F].pure(ctx.getTotalItems)

    }
}
