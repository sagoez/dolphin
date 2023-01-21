// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.outcome

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

  private[dolphin] def makeAll[F[_]: Applicative](
    result: dbclient.PersistentSubscriptionToAllStats
  ) =
    new PersistentSubscriptionToStatsOutcome[F] {
      import dolphin.concurrent.Position.*
      import dolphin.concurrent.Position
      import scala.jdk.OptionConverters.*

      /** The transaction log position of the last checkpoint. */
      def getLastCheckpointedEventPosition: F[Option[Position]] = Applicative[F].pure(
        result.getLastCheckpointedEventPosition.toScala.map(_.toScala)
      )

      /** The transaction log position of the last known event. */
      def getLastKnownEventPosition: F[Option[Position]] = Applicative[F].pure(
        result.getLastKnownEventPosition.toScala.map(_.toScala)
      )

      /** Average number of events per seconds. */
      def getAveragePerSecond: F[Int] = Applicative[F].pure(result.getAveragePerSecond)

      /** Number of events seen since last measurement on this connection. */
      def getCountSinceLastMeasurement: F[Int] = Applicative[F].pure(result.getCountSinceLastMeasurement)

      /** Number of events in the live buffer. */
      def getLiveBufferCount: F[Long] = Applicative[F].pure(result.getLiveBufferCount)

      /** Current number of outstanding messages. */
      def getOutstandingMessagesCount: F[Int] = Applicative[F].pure(result.getOutstandingMessagesCount)

      /** The current number of parked messages. */
      def getParkedMessageCount: F[Long] = Applicative[F].pure(result.getParkedMessageCount)

      /** Number of events in the read buffer. */
      def getReadBufferCount: F[Int] = Applicative[F].pure(result.getReadBufferCount)

      /** Number of events in the retry buffer. */
      def getRetryBufferCount: F[Int] = Applicative[F].pure(result.getRetryBufferCount)

      /** Current in flight messages across the persistent subscription group. */
      def getTotalInFlightMessages: F[Int] = Applicative[F].pure(result.getTotalInFlightMessages)

      /** Total number of events processed by subscription. */
      def getTotalItemsProcessed: F[Long] = Applicative[F].pure(result.getTotalItems)
    }

  private[dolphin] def makeStream[F[_]: Applicative](
    result: dbclient.PersistentSubscriptionToStreamStats
  ) =
    new PersistentSubscriptionToStatsOutcome[F] {
      import scala.jdk.OptionConverters.*

      /** The revision number of the last checkpoint. */
      def getLastCheckpointedEventRevision: F[Option[Long]] = Applicative[F].pure(
        result.getLastCheckpointedEventRevision.toScala.map(_.longValue())
      )

      /** The revision number of the last known event. */
      def getLastKnownEventRevision: F[Option[Long]] = Applicative[F].pure(
        result.getLastKnownEventRevision.toScala.map(_.longValue())
      )

      /** Average number of events per seconds. */
      def getAveragePerSecond: F[Int] = Applicative[F].pure(result.getAveragePerSecond)

      /** Number of events seen since last measurement on this connection. */
      def getCountSinceLastMeasurement: F[Int] = Applicative[F].pure(result.getCountSinceLastMeasurement)

      /** Number of events in the live buffer. */
      def getLiveBufferCount: F[Long] = Applicative[F].pure(result.getLiveBufferCount)

      /** Current number of outstanding messages. */
      def getOutstandingMessagesCount: F[Int] = Applicative[F].pure(result.getOutstandingMessagesCount)

      /** The current number of parked messages. */
      def getParkedMessageCount: F[Long] = Applicative[F].pure(result.getParkedMessageCount)

      /** Number of events in the read buffer. */
      def getReadBufferCount: F[Int] = Applicative[F].pure(result.getReadBufferCount)

      /** Number of events in the retry buffer. */
      def getRetryBufferCount: F[Int] = Applicative[F].pure(result.getRetryBufferCount)

      /** Current in flight messages across the persistent subscription group. */
      def getTotalInFlightMessages: F[Int] = Applicative[F].pure(result.getTotalInFlightMessages)

      /** Total number of events processed by subscription. */
      def getTotalItemsProcessed: F[Long] = Applicative[F].pure(result.getTotalItems)

    }
}
