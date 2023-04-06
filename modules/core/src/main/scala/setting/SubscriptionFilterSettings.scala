// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting

import java.util.concurrent.CompletableFuture

import scala.concurrent.Future
import scala.jdk.FutureConverters.*
import scala.util.{Failure, Success, Try}

import dolphin.VolatileConsumer
import dolphin.concurrent.Position
import dolphin.concurrent.Position.*

import cats.effect.kernel.Async
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
    * </br> Setting a checkpoint is a one time operation per subscription, the given callback will then live outside
    * cats-effect's runtime and will be called everytime a checkpoint is reached. For this reason, is not necessary to
    * use a [[cats.effect.std.Dispatcher]] to run the callback, but raw Java API and Scala futures are used.
    */
  def withCheckpointer(
    checkpointer: (Subscription, Position) => Future[Unit]
  ): SubscriptionFilterSettings = withCheckpointer(checkpointer, 1)

  /** Calls a callback everytime a checkpoint is reached.
    *
    * </br> Setting a checkpoint is a one time operation per subscription, the given callback will then live outside
    * cats-effect's runtime and will be called everytime a checkpoint is reached. For this reason, is not necessary to
    * use a [[cats.effect.std.Dispatcher]] to run the callback, but raw Java API and Scala futures are used. It is
    * recommended to use a [[cats.effect.std.Dispatcher]], thus the overload with a Dispatcher is preferred.
    */
  def withCheckpointer(
    checkpointer: (Subscription, Position) => Future[Unit],
    intervalMultiplierUnsigned: Int
  ): SubscriptionFilterSettings = {
    val checkpoint =
      new Checkpointer() {
        override def onCheckpoint(
          subscription: Subscription,
          position: PositionJava
        ): CompletableFuture[Void] = checkpointer(subscription, position.toScala)
          .asJava
          .toCompletableFuture
          .thenApply(_ => null.asInstanceOf[Void])
      }
    new SubscriptionFilterSettings(() =>
      subscriptionFilter().withCheckpointer(checkpoint, intervalMultiplierUnsigned)
    ) {}
  }

  /** Calls a callback everytime a checkpoint is reached.
    *
    * </br> Setting a checkpoint is a one time operation per subscription, the given callback will then live outside
    * cats-effect's runtime and will be called everytime a checkpoint is reached. For this reason, is not necessary to
    * use a [[cats.effect.std.Dispatcher]] to run the callback, but preferred.
    */
  def withCheckpointer[F[_]: Async](
    checkpointer: (VolatileConsumer[F], Position) => F[Unit],
    intervalMultiplierUnsigned: Int = 1
  )(
    dispatcher: Dispatcher[F]
  ): SubscriptionFilterSettings = {
    val checkpoint =
      new Checkpointer() {
        override def onCheckpoint(
          subscription: Subscription,
          position: PositionJava
        ): CompletableFuture[Void] = dispatcher
          .unsafeToCompletableFuture(checkpointer(VolatileConsumer.make(subscription), position.toScala))
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
    * </br> Beware of the fact that the default settings have the following caveats:
    *   - The `withEventTypeRegex`, `withStreamNameRegex`, `withEventTypePrefix` and `withStreamNamePrefix` methods are
    *     mutually exclusive. If any of them are called, the first one will take precedence.
    *   - When calling default, you must call at least one of the following methods:
    *     - `withEventTypePrefix`
    *     - `withStreamNamePrefix`
    *     - `withEventTypeRegex`
    *     - `withStreamNameRegex`
    *     - If you don't, the subscription will not receive all events.
    *
    * @return
    *   A new subscription filter settings instance [[SubscriptionFilterSettings]]
    */
  val Default: SubscriptionFilterSettings = new SubscriptionFilterSettings(() => SubscriptionFilterJava.newBuilder()) {}

}
