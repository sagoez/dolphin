// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.outcome

import dolphin.concurrent.NackAction

import cats.Applicative
import cats.syntax.traverse.*
import com.eventstore.dbclient

sealed trait PersistentSubscriptionResult extends Product with Serializable { self =>

  def value =
    self match {
      case PersistentSubscriptionResult.Id(id) => id
      case PersistentSubscriptionResult.Unit   => ()
    }
}

object PersistentSubscriptionResult {
  final case class Id(id: String) extends PersistentSubscriptionResult
  case object Unit                extends PersistentSubscriptionResult
}

sealed trait PersistentSubscription extends Product with Serializable {

  /** Acknowledges events have been successfully processed. */
  def ack(event: dbclient.ResolvedEvent): PersistentSubscription = PersistentSubscription.Ack(event)

  /** Acknowledges events have been successfully processed. */
  def ack(events: List[dbclient.ResolvedEvent]): PersistentSubscription = PersistentSubscription.AckMany(events)

  /** Returns the persistent subscription's id. */
  def getSubscriptionId: PersistentSubscription = PersistentSubscription.GetSubscriptionId

  /** Acknowledges events failed processing. */
  def nack(
    action: NackAction,
    reason: String,
    event: dbclient.ResolvedEvent
  ): PersistentSubscription = PersistentSubscription.Nack(action, reason, event)

  /** Acknowledges events failed processing. */
  def nack(
    action: NackAction,
    reason: String,
    events: List[dbclient.ResolvedEvent]
  ): PersistentSubscription = PersistentSubscription.NackMany(action, reason, events)

  /** Stops the persistent subscription. */
  def stop: PersistentSubscription = PersistentSubscription.Stop
}

object PersistentSubscription {

  final case class Ack(event: dbclient.ResolvedEvent)            extends PersistentSubscription
  final case class AckMany(events: List[dbclient.ResolvedEvent]) extends PersistentSubscription
  case object GetSubscriptionId                                  extends PersistentSubscription
  final case class Nack(action: NackAction, reason: String, event: dbclient.ResolvedEvent)
    extends PersistentSubscription
  final case class NackMany(action: NackAction, reason: String, events: List[dbclient.ResolvedEvent])
    extends PersistentSubscription
  case object Stop                                               extends PersistentSubscription
  case object Empty                                              extends PersistentSubscription

  private[dolphin] def interpreter[F[_]: Applicative](
    subscription: dbclient.PersistentSubscription,
    cmds: List[PersistentSubscription]
  ): F[List[PersistentSubscriptionResult]] = {
    def handleCommand(cmd: PersistentSubscription): F[PersistentSubscriptionResult] =
      cmd match {
        case Ack(event)                       =>
          Applicative[F].pure {
            subscription.ack(event)
            PersistentSubscriptionResult.Unit
          }
        case AckMany(events)                  =>
          import scala.jdk.CollectionConverters._
          Applicative[F].pure {
            subscription.ack(events.asJava.iterator())
            PersistentSubscriptionResult.Unit
          }
        case GetSubscriptionId                => Applicative[F].pure(PersistentSubscriptionResult.Id(subscription.getSubscriptionId()))
        case Nack(action, reason, event)      =>
          Applicative[F].pure {
            subscription.nack(action.toJava, reason, event)
            PersistentSubscriptionResult.Unit
          }
        case NackMany(action, reason, events) =>
          import scala.jdk.CollectionConverters._
          Applicative[F].pure {
            subscription.nack(action.toJava, reason, events.asJava.iterator())
            PersistentSubscriptionResult.Unit
          }
        case Stop                             =>
          Applicative[F].pure {
            subscription.stop()
            PersistentSubscriptionResult.Unit
          }
        case _                                => Applicative[F].pure(PersistentSubscriptionResult.Unit)
      }
    cmds.traverse(handleCommand)
  }

}
