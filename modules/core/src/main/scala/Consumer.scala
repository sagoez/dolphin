// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin

import cats.Applicative
import com.eventstore.dbclient.Subscription

sealed trait Consumer[F[_]] {

  /** Stops the subscription. */
  def stop: F[Unit]

  /** No operation. */
  def noop: F[Unit]
}

trait VolatileConsumer[F[_]] extends Consumer[F]

object VolatileConsumer {

  private[dolphin] def make[F[_]: Applicative](subscription: Subscription): VolatileConsumer[F] =
    new VolatileConsumer[F] {
      override def stop: F[Unit] = Applicative[F].pure(subscription.stop())

      override def noop: F[Unit] = Applicative[F].unit
    }

}

import dolphin.concurrent.NackAction

import cats.syntax.functor.*
import cats.syntax.parallel.*
import cats.{Applicative, Parallel}
import com.eventstore.dbclient.PersistentSubscription

trait PersistentConsumer[F[_]] extends Consumer[F] { self =>

  /** Stops the subscription. */
  def stop: F[Unit]

  /** Gets the subscription id. */
  def subscriptionId: F[String]

  /** Acknowledges the event. */
  def ack(event: Event): F[Unit]

  /** Acknowledges the events. */
  def ackMany(events: List[Event]): F[Unit]

  def nack(
    action: NackAction,
    event: Event,
    reason: String
  ): F[Unit]

  def nackMany(action: NackAction, events: List[Event], reason: String): F[Unit]
}

object PersistentConsumer {

  final case class Message[F[_]](
    consumer: PersistentConsumer[F],
    event: Event
  )

  private[dolphin] def make[F[_]: Applicative: Parallel](
    subscription: PersistentSubscription
  ): PersistentConsumer[F] =
    new PersistentConsumer[F] {

      override def stop: F[Unit] = Applicative[F].pure(subscription.stop())

      override def subscriptionId: F[String] = Applicative[F].pure(subscription.getSubscriptionId)

      override def ack(
        event: Event
      ): F[Unit] = Applicative[F].pure(subscription.ack(event.getResolvedEventUnsafe))

      override def ackMany(
        events: List[Event]
      ): F[Unit] = events.parTraverse(event => ack(event)).void

      override def nack(
        action: NackAction,
        event: Event,
        reason: String
      ): F[Unit] = Applicative[F].pure(subscription.nack(action.toJava, reason, event.getResolvedEventUnsafe))

      override def nackMany(
        action: NackAction,
        events: List[Event],
        reason: String
      ): F[Unit] = events.parTraverse(event => nack(action, event, reason)).void

      def noop: F[Unit] = Applicative[F].unit
    }

}
