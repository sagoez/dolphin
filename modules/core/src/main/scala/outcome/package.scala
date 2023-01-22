// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin

import dolphin.outcome.PersistentSubscriptionToSettingsOutcome.{
  PersistentSubscriptionToSettingsWithAllOutcome,
  PersistentSubscriptionToSettingsWithStreamOutcome
}
import dolphin.outcome.PersistentSubscriptionToStatsOutcome.{
  PersistentSubscriptionToStatsWithAllOutcome,
  PersistentSubscriptionToStatsWithStreamOutcome
}

package object outcome {

  type PersistentOutcomeAll[F[_]] = PersistentSubscriptionToInfoOutcome[
    F,
    PersistentSubscriptionToStatsWithAllOutcome[F],
    PersistentSubscriptionToSettingsWithAllOutcome[F]
  ]

  type PersistentOutcomeStream[F[_]] = PersistentSubscriptionToInfoOutcome[
    F,
    PersistentSubscriptionToStatsWithStreamOutcome[F],
    PersistentSubscriptionToSettingsWithStreamOutcome[F]
  ]
}
