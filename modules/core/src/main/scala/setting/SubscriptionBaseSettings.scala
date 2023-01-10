// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting

import dolphin.concurrent.StreamPosition
import dolphin.setting.BaseSettings

trait SubscriptionBaseSettings[T, A] extends BaseSettings[T] {

  /** Starts the persistent subscription from a specific revision number. */
  def fromRevision(revision: StreamPosition[A]): T

  /** Starts the persistent subscription from start. */
  def fromStart: T = fromRevision(StreamPosition.Start)

  /** Starts the persistent subscription from the end. */
  def fromEnd: T = fromRevision(StreamPosition.End)

}
