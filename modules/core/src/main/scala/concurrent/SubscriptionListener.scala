// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.concurrent

import dolphin.Event

import cats.effect.kernel.Sync
import com.eventstore.dbclient.{ResolvedEvent, Subscription, SubscriptionListener as JSubscriptionListener}
import fs2.{Chunk, Stream}

sealed abstract case class SubscriptionListener[F[_]: Sync]() { self =>

  private[dolphin] val underlying = new Underlying[F]()

  def subscription: Stream[F, Either[Throwable, Event]] = underlying.subscription

}

object SubscriptionListener {
  def default[F[_]: Sync]: SubscriptionListener[F] = new SubscriptionListener[F] {}
}

private[dolphin] final case class Underlying[F[_]: Sync]() extends JSubscriptionListener {
  private[dolphin] val queue = new java.util.concurrent.ConcurrentLinkedQueue[Either[Throwable, Event]]()
//  private val queue = Queue.bounded[F, Event](100)

  /** Called when an exception was raised when processing an event.
    */
  override def onCancelled(subscription: Subscription): Unit = {
    queue.clear()
    println("Subscription cancelled")
  }

  override def onConfirmation(subscription: Subscription): Unit = println("Subscription confirmed")

  /** Called when an exception was raised when processing an event.
    */
  override def onError(subscription: Subscription, throwable: Throwable): Unit = {
    queue.add(Left(throwable))
    println("Error processing event")
  }

  /** Called when EventStoreDB sends an event to the subscription.
    */
  override def onEvent(
    subscription: Subscription,
    event: ResolvedEvent
  ): Unit = {

    queue.add(Right(event.getOriginalEvent.getEventData))
    println("Event added to append queue")

  }

  def subscription: Stream[F, Either[Throwable, Event]] =
    Stream.unfoldChunkEval(()) { _ =>
      Sync[F].delay {
        Option(queue.poll()).map { event =>
          (Chunk.singleton(event), ())
        }
      }
    }

}
