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
import dolphin.outcome.{PersistentSubscription, ResolvedEventOutcome}
import dolphin.trace.Trace

import cats.Applicative
import cats.effect.kernel.Sync
import cats.effect.unsafe.IORuntime
import com.eventstore.dbclient.{
  PersistentSubscription as JPersistentSubscription,
  PersistentSubscriptionListener as JSubscriptionListener,
  ResolvedEvent
}
import fs2.{Chunk, Stream}

trait PersistentSubscriptionListener[F[_]] extends Product with Serializable {
  def listener: JSubscriptionListener
  def stream: Stream[F, Either[Throwable, ResolvedEventOutcome[F]]]
}

object PersistentSubscriptionListener {

  final case class WithHandler[F[_]: Trace: IOFuture: Applicative](
    onEventF: (SubscriptionId, ResolvedEvent) => F[List[PersistentSubscription]],
    onConfirmationF: SubscriptionId => F[List[PersistentSubscription]],
    onErrorF: (SubscriptionId, Throwable) => F[List[PersistentSubscription]],
    onCancelledF: SubscriptionId => F[List[PersistentSubscription]]
  )(
    implicit ec: ExecutionContext,
    runtime: IORuntime
  ) extends PersistentSubscriptionListener[F] {

    override def stream: Stream[F, Either[Throwable, ResolvedEventOutcome[F]]] = Stream.empty

    override def listener: JSubscriptionListener =
      new JSubscriptionListener {

        override def onCancelled(
          subscription: JPersistentSubscription
        ): Unit = IOFuture[F]
          .convert(onCancelledF(subscription.getSubscriptionId))
          .onComplete {
            case Success(cmd)       => PersistentSubscription.interpreter[F](subscription, cmd).toUnit
            case Failure(exception) =>
              Trace[F].error(exception, Some("PersistentSubscription onCancelled handler failed")).toUnit
          }

        override def onConfirmation(subscription: JPersistentSubscription): Unit = IOFuture[F]
          .convert(onConfirmationF(subscription.getSubscriptionId))
          .onComplete {
            case Success(cmd)       => PersistentSubscription.interpreter[F](subscription, cmd).toUnit
            case Failure(exception) =>
              Trace[F].error(exception, Some("PersistentSubscription onConfirmation handler failed")).toUnit
          }

        override def onError(subscription: JPersistentSubscription, throwable: Throwable): Unit = IOFuture[F]
          .convert(onErrorF(subscription.getSubscriptionId(), throwable))
          .onComplete {
            case Success(cmd)       => PersistentSubscription.interpreter[F](subscription, cmd).toUnit
            case Failure(exception) =>
              Trace[F].error(exception, Some("PersistentSubscription onError handler failed")).toUnit
          }

        override def onEvent(subscription: JPersistentSubscription, retryCount: Int, event: ResolvedEvent): Unit =
          IOFuture[F]
            .convert(onEventF(subscription.getSubscriptionId, event))
            .onComplete {
              case Success(cmd)       => PersistentSubscription.interpreter[F](subscription, cmd).toUnit
              case Failure(exception) =>
                Trace[F].error(exception, Some("PersistentSubscription onEvent handler failed")).toUnit
            }
      }
  }

  final case class WithStream[F[_]: Sync]() extends PersistentSubscriptionListener[F] {
    private[dolphin] val queue = new ConcurrentLinkedQueue[Either[Throwable, ResolvedEventOutcome[F]]]()

    def listener: JSubscriptionListener =
      new JSubscriptionListener {

        override def onCancelled(
          subscription: JPersistentSubscription
        ): Unit = queue.clear()

        override def onConfirmation(
          subscription: JPersistentSubscription
        ): Unit = ()

        override def onError(subscription: JPersistentSubscription, throwable: Throwable): Unit = {
          queue.add(
            Left(throwable)
          )
          ()
        }

        override def onEvent(
          subscription: JPersistentSubscription,
          retryCount: Int,
          event: ResolvedEvent
        ): Unit = {
          queue.add(
            Right(
              ResolvedEventOutcome.make(
                event
              )
            )
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
