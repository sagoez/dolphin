// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.outcome

import cats.Applicative
import com.eventstore.dbclient

// TODO: Split into separate more readable, extensible and generic implementation, hence importing inside each case class
sealed trait PersistentSubscriptionToInfoOutcome[F[_], +Stats, +Settings] {

  def stats: Stats

  def settings: Settings

  def information: PersistentSubscriptionInfoOutcome[F]

}

object PersistentSubscriptionToInfoOutcome {

  private[dolphin] def makeStream[F[_]: Applicative, Stats, Settings](
    ctx: dbclient.PersistentSubscriptionToStreamInfo
  ): PersistentSubscriptionToInfoOutcome[
    F,
    PersistentSubscriptionToStatsOutcome[F],
    PersistentSubscriptionToSettingsOutcome[F]
  ] =
    new PersistentSubscriptionToInfoOutcome[
      F,
      PersistentSubscriptionToStatsOutcome[F],
      PersistentSubscriptionToSettingsOutcome[F]
    ] {

      def stats: PersistentSubscriptionToStatsOutcome[F] = PersistentSubscriptionToStatsOutcome.makeStream(
        ctx.getStats
      )

      def settings: PersistentSubscriptionToSettingsOutcome[F] = PersistentSubscriptionToSettingsOutcome.makeStream(
        ctx.getSettings
      )

      def information: PersistentSubscriptionInfoOutcome[F] = PersistentSubscriptionInfoOutcome.make(ctx)
    }

  private[dolphin] def makeAll[F[_]: Applicative](
    ctx: dbclient.PersistentSubscriptionToAllInfo
  ): PersistentSubscriptionToInfoOutcome[
    F,
    PersistentSubscriptionToStatsOutcome[F],
    PersistentSubscriptionToSettingsOutcome[F]
  ] =
    new PersistentSubscriptionToInfoOutcome[
      F,
      PersistentSubscriptionToStatsOutcome[F],
      PersistentSubscriptionToSettingsOutcome[F]
    ] {

      def stats: PersistentSubscriptionToStatsOutcome[F] = PersistentSubscriptionToStatsOutcome.makeAll(
        ctx.getStats
      )

      def settings: PersistentSubscriptionToSettingsOutcome[F] = PersistentSubscriptionToSettingsOutcome.makeAll(
        ctx.getSettings
      )

      def information: PersistentSubscriptionInfoOutcome[F] = PersistentSubscriptionInfoOutcome.make(ctx)
    }

}
