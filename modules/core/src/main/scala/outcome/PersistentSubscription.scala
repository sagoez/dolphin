// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.outcome

import com.eventstore.dbclient

sealed trait PersistentSubscription {

  /** The source of events for the subscription. */
  def getEventSource: String

  /** The group name given on creation. */
  def getGroupName: String

  /** The current status of the subscription. */
  def getStatus: String

  /** Active connections to the subscription. */
  def getConnections: List[Connection]
}

object PersistentSubscription {

  private[dolphin] def make(
    ctx: dbclient.PersistentSubscriptionInfo
  ): PersistentSubscription =
    new PersistentSubscription {
      import scala.jdk.CollectionConverters.*

      /** The source of events for the subscription. */
      def getEventSource: String = ctx.getEventSource

      /** The group name given on creation. */
      def getGroupName: String = ctx.getGroupName

      /** The current status of the subscription. */
      def getStatus: String = ctx.getStatus

      /** Active connections to the subscription. */
      def getConnections: List[Connection] = ctx
        .getConnections
        .asScala
        .toList
        .map(value => Connection.make(value))
    }

}
