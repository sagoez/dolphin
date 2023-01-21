// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.concurrent

import java.util.concurrent.ConcurrentLinkedQueue

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

import dolphin.SubscriptionId
import dolphin.internal.syntax.IOFuture
import dolphin.internal.syntax.future.*
import dolphin.outcome.{ResolvedEventOutcome, VolatileSubscription}
import dolphin.trace.Trace

import cats.Applicative
import cats.effect.kernel.Sync
import cats.effect.unsafe.IORuntime
import com.eventstore.dbclient.{
  ResolvedEvent,
  Subscription as JSubscription,
  SubscriptionListener as JSubscriptionListener
}
import fs2.{Chunk, Stream}

sealed trait VolatileSubscriptionListener[F[_]] extends Product with Serializable {
  def listener: JSubscriptionListener

  def stream: Stream[F, Either[Throwable, ResolvedEventOutcome[F]]]
}

object VolatileSubscriptionListener {

  final case class WithHandler[F[_]: IOFuture: Trace: Applicative](
    onEventF: (SubscriptionId, ResolvedEvent) => F[List[VolatileSubscription]],
    onConfirmationF: SubscriptionId => F[List[VolatileSubscription]],
    onErrorF: (SubscriptionId, Throwable) => F[List[VolatileSubscription]],
    onCancelledF: SubscriptionId => F[List[VolatileSubscription]]
  )(
    implicit ec: ExecutionContext,
    runtime: IORuntime
  ) extends VolatileSubscriptionListener[F] {

    override def listener: JSubscriptionListener =
      new JSubscriptionListener {

        /** Called when EventStoreDB sends an event to the subscription. */
        override def onEvent(
          subscription: JSubscription,
          event: ResolvedEvent
        ): Unit = IOFuture[F].convert(onEventF(subscription.getSubscriptionId, event)).onComplete {
          case Failure(exception) => Trace[F].error(exception, Some("Subscription onEvent handler failed")).toUnit
          case Success(value)     => VolatileSubscription.interpreter[F](subscription, value).toUnit
        }

        /** Called when an exception was raised when processing an event. */
        override def onCancelled(
          subscription: JSubscription
        ): Unit = onCancelledF(subscription.getSubscriptionId).toFuture.onComplete {
          case Success(value)     => VolatileSubscription.interpreter[F](subscription, value).toUnit
          case Failure(exception) => Trace[F].error(exception, Some("Subscription onCancelled handler failed")).toUnit
        }

        override def onConfirmation(
          subscription: JSubscription
        ): Unit = onConfirmationF(subscription.getSubscriptionId).toFuture.onComplete {
          case Failure(exception) =>
            Trace[F].error(exception, Some("Subscription onConfirmation handler failed")).toUnit
          case Success(value)     => VolatileSubscription.interpreter[F](subscription, value).toUnit
        }

        /** Called when an exception was raised when processing an event. */
        override def onError(
          subscription: JSubscription,
          throwable: Throwable
        ): Unit = IOFuture[F].convert(onErrorF(subscription.getSubscriptionId, throwable)).onComplete {
          case Success(value)     => VolatileSubscription.interpreter[F](subscription, value).toUnit
          case Failure(exception) => Trace[F].error(exception, Some("Subscription onError handler failed")).toUnit
        }
      }

    override def stream: Stream[F, Either[Throwable, ResolvedEventOutcome[F]]] = Stream.empty
  }

  final case class WithStream[F[_]: Sync]() extends VolatileSubscriptionListener[F] {

    private[dolphin] val queue = new ConcurrentLinkedQueue[Either[Throwable, ResolvedEventOutcome[F]]]()

    override def listener: JSubscriptionListener =
      new JSubscriptionListener {

        /** Called when an exception was raised when processing an event. */
        override def onCancelled(subscription: JSubscription): Unit = queue.clear()

        override def onConfirmation(subscription: JSubscription): Unit = ()

        /** Called when an exception was raised when processing an event. */
        override def onError(subscription: JSubscription, throwable: Throwable): Unit = {
          queue.add(Left(throwable))
          ()
        }

        /** Called when EventStoreDB sends an event to the subscription. */
        override def onEvent(
          subscription: JSubscription,
          event: ResolvedEvent
        ): Unit = {
          queue.add(
            Right(ResolvedEventOutcome.make(event))
          )
          ()
        }

      }

    def stream: Stream[F, Either[Throwable, ResolvedEventOutcome[F]]] =
      Stream.unfoldChunkEval(()) { _ =>
        Sync[F].delay {
          Option(queue.poll()).map { event =>
            (Chunk.singleton(event), ())
          }
        }
      }
  }

}
