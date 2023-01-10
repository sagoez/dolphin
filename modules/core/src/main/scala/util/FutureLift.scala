// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.util

import java.util.concurrent.CompletableFuture

import cats.effect.kernel.Async

private[dolphin] trait FutureLift[F[_]] {
  def futureLift[A](fa: => CompletableFuture[A]): F[A]

  def delay[A](fa: => A): F[A]
}

private[dolphin] object FutureLift {

  def apply[F[_]: FutureLift]: FutureLift[F] = implicitly

  implicit def forAsync[F[_]: Async]: FutureLift[F] =
    new FutureLift[F] {

      def delay[A](fa: => A): F[A] = Async[F].delay(fa)

      def futureLift[A](fa: => CompletableFuture[A]): F[A] = Async[F].fromCompletableFuture(Async[F].delay(fa))

    }

  implicit final class FutureLiftOps[F[_]: FutureLift, A](fa: => CompletableFuture[A]) {
    def futureLift: F[A] = FutureLift[F].futureLift(fa)
  }
}
