// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.internal.builder.listener

import dolphin.Message.PersistentMessage
import dolphin.{Event, Message, PersistentConsumer}

import cats.Parallel
import cats.effect.kernel.Sync
import cats.effect.std.{Dispatcher, Queue}
import cats.effect.{Async, Resource}
import com.eventstore.dbclient.{
  PersistentSubscription => JPersistentSubscription,
  PersistentSubscriptionListener => JSubscriptionListener,
  ResolvedEvent
}
import fs2.Stream

private[dolphin] sealed trait PersistentSubscriptionListenerBuilder[F[_]] extends Product with Serializable {
  def listener: JSubscriptionListener
  def stream: Stream[F, PersistentMessage[F]]
}

private[dolphin] object PersistentSubscriptionListenerBuilder {

  final case class WithFutureHandlerBuilder[F[_]: Async: Parallel](
    handler: PersistentMessage[F] => F[Unit],
    dispatcher: Dispatcher[F]
  ) extends PersistentSubscriptionListenerBuilder[F] {

    override def stream: Stream[F, PersistentMessage[F]] = Stream.empty

    override def listener: JSubscriptionListener =
      new JSubscriptionListener {

        override def onCancelled(
          subscription: JPersistentSubscription
        ): Unit = dispatcher.unsafeRunAndForget(handler(Message.Cancelled(PersistentConsumer.make(subscription))))

        override def onConfirmation(
          subscription: JPersistentSubscription
        ): Unit = dispatcher.unsafeRunAndForget(handler(Message.Confirmation(PersistentConsumer.make(subscription))))

        override def onError(
          subscription: JPersistentSubscription,
          throwable: Throwable
        ): Unit = dispatcher.unsafeRunAndForget(
          handler(Message.Error(PersistentConsumer.make(subscription), throwable))
        )

        override def onEvent(subscription: JPersistentSubscription, retryCount: Int, event: ResolvedEvent): Unit =
          dispatcher.unsafeRunAndForget(
            handler(Message.Event(PersistentConsumer.make(subscription), Event.make(event), Some(retryCount)))
          )
      }
  }

  final case class WithStreamHandlerBuilder[F[_]: Sync: Parallel](
    queue: Queue[F, PersistentMessage[F]],
    dispatcher: Dispatcher[F]
  ) extends PersistentSubscriptionListenerBuilder[F] {

    override def listener: JSubscriptionListener =
      new JSubscriptionListener {

        /** Called when the subscription is cancelled or dropped. */
        override def onCancelled(
          subscription: JPersistentSubscription
        ): Unit = dispatcher.unsafeRunAndForget(queue.offer(Message.Cancelled(PersistentConsumer.make(subscription))))

        override def onConfirmation(
          subscription: JPersistentSubscription
        ): Unit = dispatcher.unsafeRunAndForget(
          queue.offer(Message.Confirmation(PersistentConsumer.make(subscription)))
        )

        /** Called when an exception was raised when processing an event. */
        override def onError(
          subscription: JPersistentSubscription,
          throwable: Throwable
        ): Unit = dispatcher.unsafeRunAndForget(
          queue.offer(Message.Error(PersistentConsumer.make(subscription), throwable))
        )

        /** Called when EventStoreDB sends an event to the subscription. */
        override def onEvent(
          subscription: JPersistentSubscription,
          retryCount: Int,
          event: ResolvedEvent
        ): Unit = dispatcher.unsafeRunAndForget(
          queue.offer(Message.Event(PersistentConsumer.make(subscription), Event.make(event), Some(retryCount)))
        )

      }

    def stream: Stream[F, PersistentMessage[F]] = Stream.fromQueueUnterminated(queue)

  }

  object WithStreamHandlerBuilder {

    private[dolphin] def make[F[_]: Async: Parallel]: Resource[F, WithStreamHandlerBuilder[F]] =
      for {
        queue      <- Resource.eval(Queue.unbounded[F, PersistentMessage[F]])
        dispatcher <- Dispatcher.sequential[F]
      } yield WithStreamHandlerBuilder(queue, dispatcher)
  }
}
