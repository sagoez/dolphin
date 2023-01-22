// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin

import dolphin.outcome.ResolvedEventOutcome

package object concurrent {
  type OnEvent[F[_], T]        = (T, ResolvedEventOutcome[F]) => F[T]
  type OnConfirmation[F[_], T] = T => F[T]
  type OnError[F[_], T]        = (T, Throwable) => F[T]
  type OnCancelled[F[_], T]    = T => F[T]
}
