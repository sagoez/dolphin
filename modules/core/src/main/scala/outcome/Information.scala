// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.outcome

import dolphin.outcome.Configuration.{ConfigurationWithAll, ConfigurationWithStream}
import dolphin.outcome.Stats.{StatsWithAll, StatsWithStream}

import cats.Applicative
import com.eventstore.dbclient

sealed trait Information[F[_], +Stat, +Setting] {

  def stats: Stat

  def settings: Setting

  def information: PersistentSubscription[F]

}

object Information {

  private[dolphin] def makeStream[F[_]: Applicative](
    ctx: dbclient.PersistentSubscriptionToStreamInfo
  ): FromStreamInformation[F] =
    new FromStreamInformation[F] {

      def stats: StatsWithStream[F] = Stats.makeStream(
        ctx.getStats
      )

      def settings: ConfigurationWithStream[F] = Configuration
        .makeStream(
          ctx.getSettings
        )

      def information: PersistentSubscription[F] = PersistentSubscription.make(ctx)
    }

  private[dolphin] def makeAll[F[_]: Applicative](
    ctx: dbclient.PersistentSubscriptionToAllInfo
  ): FromAllInformation[F] =
    new FromAllInformation[F] {

      def stats: StatsWithAll[F] = Stats.makeAll(
        ctx.getStats
      )

      def settings: ConfigurationWithAll[F] = Configuration.makeAll(
        ctx.getSettings
      )

      def information: PersistentSubscription[F] = PersistentSubscription.make(ctx)
    }

}
