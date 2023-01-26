// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.outcome

import cats.Applicative
import com.eventstore.dbclient

sealed trait PersistentSubscription[F[_]] {

  /** The source of events for the subscription. */
  def getEventSource: F[String]

  /** The group name given on creation. */
  def getGroupName: F[String]

  /** The current status of the subscription. */
  def getStatus: F[String]

  /** Active connections to the subscription. */
  def getConnections: List[Connection[F]]
}

object PersistentSubscription {

  private[dolphin] def make[F[_]: Applicative](
    ctx: dbclient.PersistentSubscriptionInfo
  ): PersistentSubscription[F] =
    new PersistentSubscription[F] {
      import scala.jdk.CollectionConverters.*

      /** The source of events for the subscription. */
      def getEventSource: F[String] = Applicative[F].pure(ctx.getEventSource)

      /** The group name given on creation. */
      def getGroupName: F[String] = Applicative[F].pure(ctx.getGroupName)

      /** The current status of the subscription. */
      def getStatus: F[String] = Applicative[F].pure(ctx.getStatus)

      /** Active connections to the subscription. */
      def getConnections: List[Connection[F]] = ctx
        .getConnections
        .asScala
        .toList
        .map(value => Connection.make[F](value))
    }

}
