// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.outcome

import dolphin.outcome.PersistentSubscriptionToSettingsOutcome.{
  PersistentSubscriptionToSettingsWithAllOutcome,
  PersistentSubscriptionToSettingsWithStreamOutcome
}
import dolphin.outcome.PersistentSubscriptionToStatsOutcome.{
  PersistentSubscriptionToStatsWithAllOutcome,
  PersistentSubscriptionToStatsWithStreamOutcome
}

import cats.Applicative
import com.eventstore.dbclient

// TODO: Split into separate more readable, extensible and generic implementation, hence importing inside each case class
sealed trait PersistentSubscriptionToInfoOutcome[F[_], +Stats, +Settings] {

  def stats: Stats

  def settings: Settings

  def information: PersistentSubscriptionInfoOutcome[F]

}

object PersistentSubscriptionToInfoOutcome {

  private[dolphin] def makeStream[F[_]: Applicative](
    ctx: dbclient.PersistentSubscriptionToStreamInfo
  ): PersistentOutcomeStream[F] =
    new PersistentOutcomeStream[F] {

      def stats: PersistentSubscriptionToStatsWithStreamOutcome[F] = PersistentSubscriptionToStatsOutcome.makeStream(
        ctx.getStats
      )

      def settings: PersistentSubscriptionToSettingsWithStreamOutcome[F] = PersistentSubscriptionToSettingsOutcome
        .makeStream(
          ctx.getSettings
        )

      def information: PersistentSubscriptionInfoOutcome[F] = PersistentSubscriptionInfoOutcome.make(ctx)
    }

  private[dolphin] def makeAll[F[_]: Applicative](
    ctx: dbclient.PersistentSubscriptionToAllInfo
  ): PersistentOutcomeAll[F] =
    new PersistentOutcomeAll[F] {

      def stats: PersistentSubscriptionToStatsWithAllOutcome[F] = PersistentSubscriptionToStatsOutcome.makeAll(
        ctx.getStats
      )

      def settings: PersistentSubscriptionToSettingsWithAllOutcome[F] = PersistentSubscriptionToSettingsOutcome.makeAll(
        ctx.getSettings
      )

      def information: PersistentSubscriptionInfoOutcome[F] = PersistentSubscriptionInfoOutcome.make(ctx)
    }

}
