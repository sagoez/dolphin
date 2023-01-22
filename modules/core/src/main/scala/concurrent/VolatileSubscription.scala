// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.concurrent

import cats.Applicative
import cats.syntax.apply.*
import com.eventstore.dbclient.Subscription as JSubscription

/** Non side-effecting subscription interpreter. */
sealed trait VolatileSubscription { self =>

  /** Sequentially composes two subscriptions commands. */
  def +(that: VolatileSubscription): VolatileSubscription = VolatileSubscription.And(self, that)

  /** Sequentially composes two subscriptions commands. */
  def andThen(that: VolatileSubscription): VolatileSubscription = self + that

  /** Stops the subscription. */
  def stop: VolatileSubscription = VolatileSubscription.Stop

  /** No operation. */
  def noop: VolatileSubscription = VolatileSubscription.Empty

}

private[dolphin] object VolatileSubscription {

  final case class And(left: VolatileSubscription, right: VolatileSubscription) extends VolatileSubscription
  case object Stop                                                              extends VolatileSubscription
  case object Empty                                                             extends VolatileSubscription

  private[dolphin] def make: VolatileSubscription = new VolatileSubscription {}

  private[dolphin] def interpreter[F[_]: Applicative](
    subscription: JSubscription,
    cmd: VolatileSubscription
  ): F[Unit] = {
    def handleCommand(cmd: VolatileSubscription): F[Unit] =
      cmd match {
        case And(left, right) => handleCommand(left) *> handleCommand(right)
        case Stop             => Applicative[F].pure(subscription.stop())
        case _                => Applicative[F].unit
      }
    handleCommand(cmd)
  }

}
