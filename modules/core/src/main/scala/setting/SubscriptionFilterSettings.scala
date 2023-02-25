// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting

import java.util.concurrent.CompletableFuture

import scala.util.{Failure, Success, Try}

import dolphin.VolatileConsumer
import dolphin.concurrent.Position
import dolphin.concurrent.Position.*

import cats.effect.kernel.{Async, Resource}
import cats.effect.std.Dispatcher
import com.eventstore.dbclient.{
  Checkpointer,
  Position => PositionJava,
  Subscription,
  SubscriptionFilter => SubscriptionFilterJava,
  SubscriptionFilterBuilder
}

sealed abstract case class SubscriptionFilterSettings(
  private val subscriptionFilter: () => SubscriptionFilterBuilder
) extends Product
  with Serializable {
  self =>

  private def keepOrModifyBuilder(stmt: Try[SubscriptionFilterBuilder]) =
    stmt match {
      case Success(value) => value
      case Failure(_)     => self.subscriptionFilter()
    }

  /** A string prefix to filter events based on their type. */
  def withEventTypePrefix(prefix: String): SubscriptionFilterSettings =
    new SubscriptionFilterSettings(() => keepOrModifyBuilder(Try(subscriptionFilter().addEventTypePrefix(prefix)))) {}

  /** A string prefix to filter events based on their stream name. */
  def withStreamNamePrefix(prefix: String): SubscriptionFilterSettings =
    new SubscriptionFilterSettings(() => keepOrModifyBuilder(Try(subscriptionFilter().addStreamNamePrefix(prefix)))) {}

  /** Calls a callback everytime a checkpoint is reached, with a default interval multiplier of 1.
    *
    * </br> If a checkpoint is set, a [[cats.effect.kernel.Resource]] is returned, this is because the checkpoint use a
    * [[cats.effect.std.Dispatcher]] to run the callback.
    */
  def withCheckpoint[F[_]: Async](
    checkpointer: (VolatileConsumer[F], Position) => F[Unit]
  ): Resource[F, SubscriptionFilterSettings] = withCheckpoint(checkpointer, 1)

  /** Calls a callback everytime a checkpoint is reached.
    *
    * </br> If a checkpoint is set, a [[cats.effect.kernel.Resource]] is returned, this is because the checkpoint use a
    * [[cats.effect.std.Dispatcher]] to run the callback.
    */
  def withCheckpoint[F[_]: Async](
    checkpointer: (VolatileConsumer[F], Position) => F[Unit],
    intervalMultiplierUnsigned: Int
  ): Resource[F, SubscriptionFilterSettings] = Dispatcher.sequential.map { dispatcher =>
    val checkpoint =
      new Checkpointer() {
        override def onCheckpoint(
          subscription: Subscription,
          position: PositionJava
        ): CompletableFuture[Void] = dispatcher
          .unsafeToCompletableFuture(
            checkpointer(VolatileConsumer.make(subscription), position.toScala)
          )
          .thenApply(_ => null.asInstanceOf[Void])
      }
    new SubscriptionFilterSettings(() =>
      subscriptionFilter().withCheckpointer(checkpoint, intervalMultiplierUnsigned)
    ) {}
  }

  /** The maximum number of events that are filtered out before the page is returned. */
  def withMaxWindow(maxWindow: Int): SubscriptionFilterSettings =
    new SubscriptionFilterSettings(() => subscriptionFilter().withMaxWindow(maxWindow)) {}

  /** A regex to filter events based on their stream name. */
  def withStreamNameRegex(regex: String): SubscriptionFilterSettings =
    new SubscriptionFilterSettings(() =>
      keepOrModifyBuilder(Try(subscriptionFilter().withStreamNameRegularExpression(regex)))
    ) {}

  /** A regex to filter events based on their type. */
  def withEventTypeRegex(regex: String): SubscriptionFilterSettings =
    new SubscriptionFilterSettings(() =>
      keepOrModifyBuilder(Try(subscriptionFilter().withEventTypeRegularExpression(regex)))
    ) {}

  private[setting] def build: SubscriptionFilterJava =
    Try(subscriptionFilter().withEventTypeRegularExpression("^.*$").build()) match {
      case Failure(_)     => self.subscriptionFilter().build()
      case Success(value) => value
    }

}

object SubscriptionFilterSettings {

  /** Creates a new [[SubscriptionFilterSettings]] instance
    *
    * Beware of the fact that the default settings have the following caveats:
    *   - The `withEventTypeRegex`, `withStreamNameRegex`, `withEventTypePrefix` and `withStreamNamePrefix` methods are
    *     mutually exclusive. If any of them are called, the first one will take precedence.
    *   - When calling default, you must call at least one of the following methods:
    *   - `withEventTypePrefix`
    *   - `withStreamNamePrefix`
    *   - `withEventTypeRegex`
    *   - `withStreamNameRegex`
    *   - If you don't, the subscription will not receive all events.
    *
    * @return
    *   A new subscription filter settings instance [[SubscriptionFilterSettings]]
    */
  val Default: SubscriptionFilterSettings = new SubscriptionFilterSettings(() => SubscriptionFilterJava.newBuilder()) {}

}
