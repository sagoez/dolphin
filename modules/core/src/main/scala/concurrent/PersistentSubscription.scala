// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.concurrent

import scala.jdk.CollectionConverters.*

import dolphin.outcome.ResolvedEventOutcome

import cats.Applicative
import cats.syntax.apply.*
import com.eventstore.dbclient

/** Non side-effecting subscription interpreter. */
sealed trait PersistentSubscription { self =>

  /** Sequentially composes two subscriptions commands. */
  def +(that: PersistentSubscription): PersistentSubscription = PersistentSubscription.And(self, that)

  /** Sequentially composes two subscriptions commands. */
  def andThen(that: PersistentSubscription): PersistentSubscription = self + that

  /** Acknowledges events have been successfully processed. */
  def ack[F[_]](event: ResolvedEventOutcome[F]): PersistentSubscription = PersistentSubscription.Ack(event)

  /** Acknowledges events have been successfully processed. */
  def ack[F[_]](events: List[ResolvedEventOutcome[F]]): PersistentSubscription = PersistentSubscription.AckMany(events)

  /** Acknowledges events failed processing. */
  def nack[F[_]](
    action: NackAction,
    reason: String,
    event: ResolvedEventOutcome[F]
  ): PersistentSubscription = PersistentSubscription.Nack(action, reason, event)

  /** Acknowledges events failed processing. */
  def nack[F[_]](
    action: NackAction,
    reason: String,
    events: List[ResolvedEventOutcome[F]]
  ): PersistentSubscription = PersistentSubscription.NackMany(action, reason, events)

  /** Stops the persistent subscription. */
  def stop: PersistentSubscription = PersistentSubscription.Stop

  /** No operation. */
  def noop: PersistentSubscription = PersistentSubscription.Empty

}

private[dolphin] object PersistentSubscription {

  final case class Ack[F[_]](event: ResolvedEventOutcome[F])                        extends PersistentSubscription
  final case class AckMany[F[_]](events: List[ResolvedEventOutcome[F]])             extends PersistentSubscription
  final case class Nack[F[_]](action: NackAction, reason: String, event: ResolvedEventOutcome[F])
    extends PersistentSubscription
  final case class NackMany[F[_]](action: NackAction, reason: String, events: List[ResolvedEventOutcome[F]])
    extends PersistentSubscription
  case object Stop                                                                  extends PersistentSubscription
  case object Empty                                                                 extends PersistentSubscription
  final case class And(left: PersistentSubscription, right: PersistentSubscription) extends PersistentSubscription

  private[dolphin] def make: PersistentSubscription = new PersistentSubscription {}

  private[dolphin] def interpreter[F[_]: Applicative](
    subscription: dbclient.PersistentSubscription,
    cmd: PersistentSubscription
  ): F[Unit] = {
    def handleCommand(cmd: PersistentSubscription): F[Unit] =
      cmd match {
        case Ack(event)                       => Applicative[F].pure(subscription.ack(event.getResolvedEventUnsafe))
        case AckMany(events)                  =>
          Applicative[F].pure(subscription.ack(events.map(_.getResolvedEventUnsafe).asJava.iterator()))
        case Nack(action, reason, event)      =>
          Applicative[F].pure(subscription.nack(action.toJava, reason, event.getResolvedEventUnsafe))
        case NackMany(action, reason, events) =>
          Applicative[F].pure(
            subscription.nack(action.toJava, reason, events.map(_.getResolvedEventUnsafe).asJava.iterator())
          )
        case And(left, right)                 => handleCommand(left) *> handleCommand(right)
        case Stop                             => Applicative[F].pure(subscription.stop())
        case _                                => Applicative[F].unit
      }
    handleCommand(cmd)
  }

}
