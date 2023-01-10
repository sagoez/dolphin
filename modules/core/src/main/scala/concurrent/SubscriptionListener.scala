// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.concurrent

import java.util.concurrent.ConcurrentLinkedQueue

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

import dolphin.Event
import dolphin.syntax.IOFuture
import dolphin.syntax.future.*
import dolphin.util.Trace

import cats.effect.kernel.Sync
import cats.effect.unsafe.IORuntime
import com.eventstore.dbclient.{ResolvedEvent, Subscription, SubscriptionListener as JSubscriptionListener}
import fs2.{Chunk, Stream}

sealed trait SubscriptionListener[F[_]] extends Product with Serializable {
  def java: JSubscriptionListener

  def subscription: Stream[F, Either[Throwable, Event]]
}

object SubscriptionListener {

  final case class WithHandler[F[_]: IOFuture: Trace](
    onEventF: ResolvedEvent => F[Unit],
    onConfirmationF: F[Unit],
    onErrorF: Throwable => F[Unit],
    onCancelledF: F[Unit]
  )(
    implicit ec: ExecutionContext
  ) extends SubscriptionListener[F] {

    override def java: JSubscriptionListener =
      new JSubscriptionListener {

        implicit val runtime: IORuntime = cats.effect.unsafe.implicits.global

        /** Called when EventStoreDB sends an event to the subscription.
          */
        override def onEvent(
          subscription: Subscription,
          event: ResolvedEvent
        ): Unit = IOFuture[F].convert(onEventF(event)).onComplete {
          case Failure(exception) => onError(subscription, exception)
          case Success(_)         => Trace[F].trace("onEvent success").toUnit
        }

        /** Called when an exception was raised when processing an event.
          */
        override def onCancelled(subscription: Subscription): Unit = onCancelledF.toFuture.onComplete {
          case Success(_)         => ()
          case Failure(exception) => Trace[F].error(exception, Some("SubscriptionListener.onCancelled")).toUnit
        }

        override def onConfirmation(subscription: Subscription): Unit = onConfirmationF.toFuture.onComplete {
          case Failure(exception) => Trace[F].error(exception, Some("onConfirmation")).toUnit
          case Success(_)         => ()
        }

        /** Called when an exception was raised when processing an event.
          */
        override def onError(
          subscription: Subscription,
          throwable: Throwable
        ): Unit = IOFuture[F].convert(onErrorF(throwable)).onComplete {
          case Success(_)         => ()
          case Failure(exception) => Trace[F].error(exception, Some("Error while handling error")).toUnit
        }
      }

    override def subscription: Stream[F, Either[Throwable, Event]] = Stream.empty
  }

  final case class WithStream[F[_]: Sync]() extends SubscriptionListener[F] {

    private[dolphin] val queue = new ConcurrentLinkedQueue[Either[Throwable, Event]]()

    override def java: JSubscriptionListener =
      new JSubscriptionListener {

        /** Called when an exception was raised when processing an event.
          */
        override def onCancelled(subscription: Subscription): Unit = queue.clear()

        override def onConfirmation(subscription: Subscription): Unit = ()

        /** Called when an exception was raised when processing an event.
          */
        override def onError(subscription: Subscription, throwable: Throwable): Unit = {
          queue.add(Left(throwable))
          ()
        }

        /** Called when EventStoreDB sends an event to the subscription.
          */
        override def onEvent(
          subscription: Subscription,
          event: ResolvedEvent
        ): Unit = {
          queue.add(Right(event.getOriginalEvent.getEventData))
          ()
        }

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

}
