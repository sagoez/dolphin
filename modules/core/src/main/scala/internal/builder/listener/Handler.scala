// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.internal.builder.listener

import dolphin.concurrent.{OnCancelled, OnConfirmation, OnError, OnEvent}

private[dolphin] trait Handler[F[_], T] {
  def onEventF: OnEvent[F, T]

  def onConfirmationF: OnConfirmation[F, T]

  def onErrorF: OnError[F, T]

  def onCancelledF: OnCancelled[F, T]
}
