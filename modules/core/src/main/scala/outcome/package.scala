// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin

import dolphin.outcome.Configuration.{ConfigurationWithAll, ConfigurationWithStream}
import dolphin.outcome.Stats.{StatsWithAll, StatsWithStream}

package object outcome {

  type FromAllInformation[F[_]] = Information[
    F,
    StatsWithAll[F],
    ConfigurationWithAll[F]
  ]

  type FromStreamInformation[F[_]] = Information[
    F,
    StatsWithStream[F],
    ConfigurationWithStream[F]
  ]
}
