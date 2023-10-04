// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.internal.builder.listener

import dolphin.Message.VolatileMessage
import dolphin.{Event, Message, MessageHandler, VolatileConsumer}

import cats.Applicative
import cats.effect.std.{Dispatcher, Queue}
import cats.effect.{Async, Resource}
import com.eventstore.dbclient.{
  ResolvedEvent,
  Subscription as JSubscription,
  SubscriptionListener as JSubscriptionListener
}
import fs2.Stream

private[dolphin] sealed trait VolatileSubscriptionListenerBuilder[F[_]] extends Product with Serializable {

  def listener: JSubscriptionListener

  // Keep this for now, maybe we can use it later
  def stream: Stream[F, VolatileMessage[F]]
}

private[dolphin] object VolatileSubscriptionListenerBuilder {

  final case class WithFutureHandlerBuilder[F[_]: Async](
    handler: MessageHandler[F, VolatileMessage[F]],
    dispatcher: Dispatcher[F]
  ) extends VolatileSubscriptionListenerBuilder[F] {

    override def listener: JSubscriptionListener =
      new JSubscriptionListener {

        // Use dispatcher and run performance to see which one is better

        /** Called when EventStoreDB sends an event to the subscription. */
        override def onEvent(
          subscription: JSubscription,
          event: ResolvedEvent
        ): Unit = dispatcher.unsafeRunAndForget(
          handler(Message.Event(VolatileConsumer.make(subscription), Event.make(event)))
        )

        override def onCancelled(
          subscription: JSubscription
        ): Unit = dispatcher.unsafeRunAndForget(
          handler(Message.Cancelled(VolatileConsumer.make(subscription)))
        )

        override def onConfirmation(
          subscription: JSubscription
        ): Unit = dispatcher.unsafeRunAndForget(
          handler(Message.Confirmation(VolatileConsumer.make(subscription)))
        )

        /** Called when an exception was raised when processing an event. */
        override def onError(
          subscription: JSubscription,
          throwable: Throwable
        ): Unit = dispatcher.unsafeRunAndForget(
          handler(Message.Error(VolatileConsumer.make(subscription), throwable))
        )

      }

    override def stream: Stream[F, VolatileMessage[F]] = Stream.empty
  }

  // Keep this for now, maybe we can use it later
  final case class WithStreamHandlerBuilder[F[_]: Applicative](
    queue: Queue[F, VolatileMessage[F]],
    dispatcher: Dispatcher[F]
  ) extends VolatileSubscriptionListenerBuilder[F] {

    override def listener: JSubscriptionListener =
      new JSubscriptionListener {

        /** Called when the subscription is cancelled or dropped. */
        override def onCancelled(
          subscription: JSubscription
        ): Unit = dispatcher.unsafeRunAndForget(
          queue.offer(Message.Cancelled[F, VolatileConsumer[F]](VolatileConsumer.make(subscription)))
        )

        override def onConfirmation(
          subscription: JSubscription
        ): Unit = dispatcher.unsafeRunAndForget(queue.offer(Message.Confirmation(VolatileConsumer.make(subscription))))

        /** Called when an exception was raised when processing an event. */
        override def onError(subscription: JSubscription, throwable: Throwable): Unit = dispatcher.unsafeRunAndForget(
          queue.offer(Message.Error(VolatileConsumer.make(subscription), throwable))
        )

        /** Called when EventStoreDB sends an event to the subscription. */
        override def onEvent(
          subscription: JSubscription,
          event: ResolvedEvent
        ): Unit = dispatcher.unsafeRunAndForget(
          queue.offer(Message.Event(VolatileConsumer.make(subscription), Event.make(event)))
        )

      }

    def stream: Stream[F, VolatileMessage[F]] = Stream.fromQueueUnterminated(queue)

  }

  object WithStreamHandlerBuilder {

    private[dolphin] def make[F[_]: Async]: Resource[F, WithStreamHandlerBuilder[F]] =
      for {
        queue      <- Resource.eval(Queue.unbounded[F, VolatileMessage[F]])
        dispatcher <- Dispatcher.sequential[F]
      } yield WithStreamHandlerBuilder(queue, dispatcher)
  }
}
