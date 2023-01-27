// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.outcome

import cats.Applicative
import com.eventstore.dbclient

sealed trait Connection[F[_]] {

  /** Number of available slots. */
  def getAvailableSlots: F[Long]

  /** Average events per second on this connection. */
  def getAverageItemsPerSecond: F[Double]

  /** Connection name. */
  def getConnectionName: F[String]

  /** Number of items seen since last measurement on this connection. */
  def getCountSinceLastMeasurement: F[Long]

  /** Timing measurements for the connection. */
  def getExtraStatistics: F[Map[String, Long]]

  /** Origin of this connection. */
  def getFrom: F[String]

  /** Number of in flight messages on this connection. */
  def getInFlightMessages: F[Long]

  /** Total items on this connection. */
  def getTotalItems: F[Long]

  /** Connection's username. */
  def getUsername: F[String]

}

object Connection {

  private[dolphin] def make[F[_]: Applicative](
    ctx: dbclient.PersistentSubscriptionConnectionInfo
  ): Connection[F] =
    new Connection[F] {
      import scala.jdk.CollectionConverters.*

      /** Number of available slots. */
      def getAvailableSlots: F[Long] = Applicative[F].pure(ctx.getAvailableSlots)

      /** Average events per second on this connection. */
      def getAverageItemsPerSecond: F[Double] = Applicative[F].pure(ctx.getAverageItemsPerSecond)

      /** Connection name. */
      def getConnectionName: F[String] = Applicative[F].pure(ctx.getConnectionName)

      /** Number of items seen since last measurement on this connection. */
      def getCountSinceLastMeasurement: F[Long] = Applicative[F].pure(ctx.getCountSinceLastMeasurement)

      /** Timing measurements for the connection. */
      def getExtraStatistics: F[Map[String, Long]] = Applicative[F].pure(
        ctx.getExtraStatistics.asScala.map { case (key, value) => (key, value.longValue()) }.toMap
      )

      /** Origin of this connection. */
      def getFrom: F[String] = Applicative[F].pure(ctx.getFrom)

      /** Number of in flight messages on this connection. */
      def getInFlightMessages: F[Long] = Applicative[F].pure(ctx.getInFlightMessages)

      /** Total items on this connection. */
      def getTotalItems: F[Long] = Applicative[F].pure(ctx.getTotalItems)

      /** Connection's username. */
      def getUsername: F[String] = Applicative[F].pure(ctx.getUsername)
    }

}
