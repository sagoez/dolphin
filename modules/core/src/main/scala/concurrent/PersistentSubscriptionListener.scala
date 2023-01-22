// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.concurrent

import java.util.concurrent.ConcurrentLinkedQueue

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

import dolphin.internal.syntax.IOFuture
import dolphin.outcome.ResolvedEventOutcome
import dolphin.trace.Trace

import cats.Applicative
import cats.effect.kernel.Sync
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
    onEventF: OnEvent[F, PersistentSubscription],
    onConfirmationF: OnConfirmation[F, PersistentSubscription],
    onErrorF: OnError[F, PersistentSubscription],
    onCancelledF: OnCancelled[F, PersistentSubscription]
  )(
    implicit ec: ExecutionContext
  ) extends PersistentSubscriptionListener[F] {

    private val ps = PersistentSubscription.make

    // TODO: Figure out how to get rid of this
    import cats.effect.unsafe.implicits.global
    override def stream: Stream[F, Either[Throwable, ResolvedEventOutcome[F]]] = Stream.empty

    override def listener: JSubscriptionListener =
      new JSubscriptionListener {

        override def onCancelled(
          subscription: JPersistentSubscription
        ): Unit = IOFuture[F]
          .convert(onCancelledF(ps))
          .onComplete {
            case Success(cmd)       => IOFuture[F].convertUnit(PersistentSubscription.interpreter[F](subscription, cmd))
            case Failure(exception) =>
              IOFuture[F].convertUnit(
                Trace[F].error(exception, Some("PersistentSubscription onCancelled handler failed"))
              )
          }

        override def onConfirmation(subscription: JPersistentSubscription): Unit = IOFuture[F]
          .convert(onConfirmationF(ps))
          .onComplete {
            case Success(cmd)       => IOFuture[F].convertUnit(PersistentSubscription.interpreter[F](subscription, cmd))
            case Failure(exception) =>
              IOFuture[F].convertUnit(
                Trace[F].error(exception, Some("PersistentSubscription onConfirmation handler failed"))
              )
          }

        override def onError(subscription: JPersistentSubscription, throwable: Throwable): Unit = IOFuture[F]
          .convert(onErrorF(ps, throwable))
          .onComplete {
            case Success(cmd)       => IOFuture[F].convertUnit(PersistentSubscription.interpreter[F](subscription, cmd))
            case Failure(exception) =>
              IOFuture[F].convertUnit(Trace[F].error(exception, Some("PersistentSubscription onError handler failed")))
          }

        override def onEvent(subscription: JPersistentSubscription, retryCount: Int, event: ResolvedEvent): Unit =
          IOFuture[F]
            .convert(onEventF(ps, ResolvedEventOutcome.make(event)))
            .onComplete {
              case Success(cmd)       => IOFuture[F].convertUnit(PersistentSubscription.interpreter[F](subscription, cmd))
              case Failure(exception) =>
                IOFuture[F].convertUnit(
                  Trace[F].error(exception, Some("PersistentSubscription onEvent handler failed"))
                )
            }
      }
  }

  final case class WithStreamHandler[F[_]: Sync]() extends PersistentSubscriptionListener[F] {
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
