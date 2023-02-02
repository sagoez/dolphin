// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.outcome

import dolphin.outcome.Configuration.{ConfigurationWithAll, ConfigurationWithStream}
import dolphin.outcome.Stats.{StatsWithAll, StatsWithStream}

import com.eventstore.dbclient

sealed trait Information[+Stat, +Setting] {

  def stats: Stat

  def settings: Setting

  def information: PersistentSubscription

}

object Information {

  private[dolphin] def makeStream(
    ctx: dbclient.PersistentSubscriptionToStreamInfo
  ): FromStreamInformation =
    new FromStreamInformation {

      def stats: StatsWithStream = Stats.makeStream(
        ctx.getStats
      )

      def settings: ConfigurationWithStream = Configuration
        .makeStream(
          ctx.getSettings
        )

      def information: PersistentSubscription = PersistentSubscription.make(ctx)
    }

  private[dolphin] def makeAll(
    ctx: dbclient.PersistentSubscriptionToAllInfo
  ): FromAllInformation =
    new FromAllInformation {

      def stats: StatsWithAll = Stats.makeAll(
        ctx.getStats
      )

      def settings: ConfigurationWithAll = Configuration.makeAll(
        ctx.getSettings
      )

      def information: PersistentSubscription = PersistentSubscription.make(ctx)
    }

}
