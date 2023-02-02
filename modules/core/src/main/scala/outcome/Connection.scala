// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.outcome

import com.eventstore.dbclient

sealed trait Connection {

  /** Number of available slots. */
  def getAvailableSlots: Long

  /** Average events per second on this connection. */
  def getAverageItemsPerSecond: Double

  /** Connection name. */
  def getConnectionName: String

  /** Number of items seen since last measurement on this connection. */
  def getCountSinceLastMeasurement: Long

  /** Timing measurements for the connection. */
  def getExtraStatistics: Map[String, Long]

  /** Origin of this connection. */
  def getFrom: String

  /** Number of in flight messages on this connection. */
  def getInFlightMessages: Long

  /** Total items on this connection. */
  def getTotalItems: Long

  /** Connection's username. */
  def getUsername: String

}

object Connection {

  private[dolphin] def make(
    ctx: dbclient.PersistentSubscriptionConnectionInfo
  ): Connection =
    new Connection {
      import scala.jdk.CollectionConverters.*

      /** Number of available slots. */
      def getAvailableSlots: Long = ctx.getAvailableSlots

      /** Average events per second on this connection. */
      def getAverageItemsPerSecond: Double = ctx.getAverageItemsPerSecond

      /** Connection name. */
      def getConnectionName: String = ctx.getConnectionName

      /** Number of items seen since last measurement on this connection. */
      def getCountSinceLastMeasurement: Long = ctx.getCountSinceLastMeasurement

      /** Timing measurements for the connection. */
      def getExtraStatistics: Map[String, Long] =
        ctx.getExtraStatistics.asScala.map { case (key, value) => (key, value.longValue()) }.toMap

      /** Origin of this connection. */
      def getFrom: String = ctx.getFrom

      /** Number of in flight messages on this connection. */
      def getInFlightMessages: Long = ctx.getInFlightMessages

      /** Total items on this connection. */
      def getTotalItems: Long = ctx.getTotalItems

      /** Connection's username. */
      def getUsername: String = ctx.getUsername
    }

}
