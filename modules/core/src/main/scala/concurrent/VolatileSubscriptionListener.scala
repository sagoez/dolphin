// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.concurrent

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

import dolphin.internal.syntax.IOFuture
import dolphin.outcome.ResolvedEventOutcome
import dolphin.trace.Trace

import cats.Applicative
import cats.effect.kernel.Sync
import com.eventstore.dbclient.{
  ResolvedEvent,
  Subscription as JSubscription,
  SubscriptionListener as JSubscriptionListener
}
import fs2.{Chunk, Stream}
// import scala.concurrent.duration.*

sealed trait VolatileSubscriptionListener[F[_]] extends Product with Serializable {

  def listener: JSubscriptionListener

  def stream: Stream[F, SubscriptionState[ResolvedEventOutcome[F]]]
}

object VolatileSubscriptionListener {

  final case class WithHandler[F[_]: IOFuture: Trace: Applicative](
    onEventF: OnEvent[F, VolatileSubscription],
    onConfirmationF: OnConfirmation[F, VolatileSubscription],
    onErrorF: OnError[F, VolatileSubscription],
    onCancelledF: OnCancelled[F, VolatileSubscription]
  )(
    implicit ec: ExecutionContext
  ) extends VolatileSubscriptionListener[F] {

    // TODO: Figure out how to get rid of this
    import cats.effect.unsafe.implicits.global

    override def listener: JSubscriptionListener =
      new JSubscriptionListener {

        private val vs = VolatileSubscription.make

        /** Called when EventStoreDB sends an event to the subscription. */
        override def onEvent(
          subscription: JSubscription,
          event: ResolvedEvent
        ): Unit = IOFuture[F]
          .convert(onEventF(vs, ResolvedEventOutcome.make(event)))
          .onComplete {
            case Failure(exception) =>
              IOFuture[F].convertUnit(Trace[F].error(exception, Some("Subscription onEvent handler failed")))
            case Success(value)     => IOFuture[F].convertUnit(VolatileSubscription.interpreter[F](subscription, value))
          }

        /** Called when the subscription is cancelled or dropped. */
        override def onCancelled(
          subscription: JSubscription
        ): Unit = IOFuture[F].convert(onCancelledF(vs)).onComplete {
          case Success(value)     => IOFuture[F].convertUnit(VolatileSubscription.interpreter[F](subscription, value))
          case Failure(exception) =>
            IOFuture[F].convertUnit(Trace[F].error(exception, Some("Subscription onCancelled handler failed")))
        }

        override def onConfirmation(
          subscription: JSubscription
        ): Unit = IOFuture[F].convert(onConfirmationF(vs)).onComplete {
          case Failure(exception) =>
            IOFuture[F].convertUnit(Trace[F].error(exception, Some("Subscription onConfirmation handler failed")))
          case Success(value)     => IOFuture[F].convertUnit(VolatileSubscription.interpreter[F](subscription, value))
        }

        /** Called when an exception was raised when processing an event. */
        override def onError(
          subscription: JSubscription,
          throwable: Throwable
        ): Unit = IOFuture[F].convert(onErrorF(vs, throwable)).onComplete {
          case Success(value)     => IOFuture[F].convertUnit(VolatileSubscription.interpreter[F](subscription, value))
          case Failure(exception) =>
            IOFuture[F].convertUnit(Trace[F].error(exception, Some("Subscription onError handler failed")))
        }
      }

    override def stream: Stream[F, SubscriptionState[ResolvedEventOutcome[F]]] = Stream.empty
  }

  final case class WithStreamHandler[F[_]: Sync]() extends VolatileSubscriptionListener[F] {

    private[dolphin] val queue = SubscriptionState.concurrentLinkedQueue[ResolvedEventOutcome[F]]

    override def listener: JSubscriptionListener =
      new JSubscriptionListener {

        /** Called when the subscription is cancelled or dropped. */
        override def onCancelled(subscription: JSubscription): Unit = {
          queue.add(SubscriptionState.Cancelled)
          ()
        }

        override def onConfirmation(subscription: JSubscription): Unit = {
          queue.add(SubscriptionState.Empty)
          ()
        }

        /** Called when an exception was raised when processing an event. */
        override def onError(subscription: JSubscription, throwable: Throwable): Unit = {
          queue.add(SubscriptionState.Error(throwable))
          ()
        }

        /** Called when EventStoreDB sends an event to the subscription. */
        override def onEvent(
          subscription: JSubscription,
          event: ResolvedEvent
        ): Unit = {
          queue.add(
            SubscriptionState.Event(ResolvedEventOutcome.make(event))
          )
          ()
        }

      }

    def stream: Stream[F, SubscriptionState[ResolvedEventOutcome[F]]] =
      Stream.unfoldChunkEval(()) { _ =>
        Sync[F].delay {
          Option(queue.poll()).map { event =>
            (Chunk.singleton(event), ())
          }
        }
      }
  }

}
