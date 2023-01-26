// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting

import java.util.concurrent.CompletableFuture

import scala.util.{Failure, Success, Try}

import dolphin.{CommitUnsigned, PrepareUnsigned}

import com.eventstore.dbclient.{
  Checkpointer,
  Position,
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

  /** Calls a callback everytime a checkpoint is reached. */
  def withCheckpoint(
    onCheckpointF: (CommitUnsigned, PrepareUnsigned) => Unit
  ): SubscriptionFilterSettings = withCheckpoint(onCheckpointF, 1)

  /** Calls a callback everytime a checkpoint is reached. */
  def withCheckpoint(
    onCheckpointF: (CommitUnsigned, PrepareUnsigned) => Unit,
    intervalMultiplierUnsigned: Int
  ): SubscriptionFilterSettings = {
    // TODO: Use Dispatcher to run the callback

    val checkpoint: Checkpointer =
      new Checkpointer() {
        override def onCheckpoint(subscription: Subscription, position: Position): CompletableFuture[Void] = {

          onCheckpointF(position.getCommitUnsigned, position.getPrepareUnsigned)

          null
        }

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
