// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin

import dolphin.builder.client.PersistentClientBuilder
import dolphin.builder.session.PersistentSessionBuilder
import dolphin.result.Result.{
  PersistentSubscriptionInfoResult,
  PersistentSubscriptionToAllInfoResult,
  PersistentSubscriptionToStreamInfoResult
}
import dolphin.setting.{
  CreatePersistentSubscriptionToAllSettings,
  CreatePersistentSubscriptionToStreamSettings,
  DeletePersistentSubscriptionSettings,
  EventStoreSettings,
  GetPersistentSubscriptionInfoSettings,
  ListPersistentSubscriptionsSettings,
  ReplayParkedMessagesSettings,
  RestartPersistentSubscriptionSubsystemSettings,
  UpdatePersistentSubscriptionToAllSettings
}
import dolphin.util.{FutureLift, Trace}

import cats.effect.kernel.{MonadCancelThrow, Resource}
import fs2.Stream
import sourcecode.{File, Line}

/** The main entry point for the EventStoreDB client.
  *
  * Represents EventStoreDB client for stream operations. A client instance maintains a two-way communication to
  * EventStoreDB. Many threads can use the EventStoreDB client simultaneously, or a single thread can make many
  * asynchronous requests.
  *
  * Persistent subscriptions are similar to catch-up subscriptions, but there are two key differences:
  *
  *   - The subscription checkpoint is maintained by the server. It means that when your client reconnects to the
  *     persistent subscription, it will automatically resume from the last known position.
  *
  *   - It's possible to connect more than one event consumer to the same persistent subscription. In that case, the
  *     server will load-balance the consumers, depending on the defined strategy, and distribute the events to them.
  *
  * Because of those, persistent subscriptions are defined as subscription groups that are defined and maintained by the
  * server. Consumer then connect to a particular subscription group, and the server starts sending event to the
  * consumer.
  *
  * You can read more about persistent subscriptions in the server <a
  * href="https://developers.eventstore.com/clients/grpc/persistent-subscriptions.html#creating-a-subscription-group">documentation</a>.
  */
trait PersistentSession[F[_]] extends Serializable { self =>

  /** Creates a persistent subscription group on the <b>all</b> stream. Persistent subscriptions are special kind of
    * subscription where the server remembers the state of the subscription. This allows for many different modes of
    * operations compared to a regular or catchup subscription where the client holds the subscription state. Persistent
    * subscriptions don't guarantee ordering and unlike catchup-subscriptions, they start from the end of stream by
    * default.
    *
    * This method is available for EventStoreDB 21.10.* and above.
    *
    * @param subscriptionGroupName
    *   group's name options
    * @param options
    *   create persistent subscription request's options.
    */
  def createToAll(
    subscriptionGroupName: String,
    options: CreatePersistentSubscriptionToAllSettings
  ): F[Unit]

  /** Creates a persistent subscription group on the <b>all</b> stream. Persistent subscriptions are special kind of
    * subscription where the server remembers the state of the subscription. This allows for many different modes of
    * operations compared to a regular or catchup subscription where the client holds the subscription state. Persistent
    * subscriptions don't guarantee ordering and unlike catchup-subscriptions, they start from the end of stream by
    * default.
    *
    * This method is available for EventStoreDB 21.10.* and above.
    *
    * @param subscriptionGroupName
    *   group's name options
    */
  def createToAll(
    subscriptionGroupName: String
  ): F[Unit]

  /** Creates a persistent subscription group on a stream.
    * @param streamName
    *   name of the stream
    * @param subscriptionGroupName
    *   group's name options
    */
  def createToStream(
    streamName: String,
    subscriptionGroupName: String
  ): F[Unit]

  /** Creates a persistent subscription group on a stream.
    * @param streamName
    *   name of the stream
    * @param subscriptionGroupName
    *   group's name options
    * @param options
    *   create persistent subscription request's options.
    */
  def createToStream(
    streamName: String,
    subscriptionGroupName: String,
    options: CreatePersistentSubscriptionToStreamSettings
  ): F[Unit]

  /** Deletes a persistent subscription group on the <b>all</b> stream.
    * @param subscriptionGroupName
    *   group's name options
    */
  def deleteToAll(
    subscriptionGroupName: String
  ): F[Unit]

  /** Deletes a persistent subscription group on the <b>all</b> stream.
    * @param subscriptionGroupName
    *   group's name options
    * @param options
    *   delete persistent subscription request's options.
    */
  def deleteToAll(
    subscriptionGroupName: String,
    options: DeletePersistentSubscriptionSettings
  ): F[Unit]

  /** Deletes a persistent subscription group on a stream.
    * @param streamName
    *   name of the stream
    * @param subscriptionGroupName
    *   group's name options
    */
  def deleteToStream(
    streamName: String,
    subscriptionGroupName: String
  ): F[Unit]

  /** Deletes a persistent subscription group on a stream.
    * @param streamName
    *   name of the stream
    * @param subscriptionGroupName
    *   group's name options
    * @param options
    *   delete persistent subscription request's options.
    */
  def deleteToStream(
    streamName: String,
    subscriptionGroupName: String,
    options: DeletePersistentSubscriptionSettings
  ): F[Unit]

  /** Gets a specific persistent subscription info to the <b>\$all</b> stream. */
  def getInfoToAll(
    subscriptionGroupName: String
  ): F[Option[PersistentSubscriptionToAllInfoResult[F]]]

  /** Gets a specific persistent subscription info to the <b>\$all</b> stream. */
  def getInfoToAll(
    subscriptionGroupName: String,
    options: GetPersistentSubscriptionInfoSettings
  ): F[Option[PersistentSubscriptionToAllInfoResult[F]]]

  /** Gets a specific persistent subscription info to a stream. */
  def getInfoToStream(
    streamName: String,
    subscriptionGroupName: String
  ): F[Option[PersistentSubscriptionToStreamInfoResult[F]]]

  /** Gets a specific persistent subscription info to a stream. */
  def getInfoToStream(
    streamName: String,
    subscriptionGroupName: String,
    options: GetPersistentSubscriptionInfoSettings
  ): F[Option[PersistentSubscriptionToStreamInfoResult[F]]]

  /** Lists all existing persistent subscriptions. */
  def listAll: F[List[PersistentSubscriptionInfoResult[F]]]

  /** Lists all existing persistent subscriptions. */
  def listAll(
    options: ListPersistentSubscriptionsSettings
  ): F[List[PersistentSubscriptionInfoResult[F]]]

  /** Lists all persistent subscriptions of a specific to the <b>\$all</b> stream. */
  def listToAll: F[List[PersistentSubscriptionToAllInfoResult[F]]]

  /** Lists all persistent subscriptions of a specific to the <b>\$all</b> stream. */
  def listToAll(
    options: ListPersistentSubscriptionsSettings
  ): F[List[PersistentSubscriptionToAllInfoResult[F]]]

  /** Lists all persistent subscriptions of a specific to the <b>\$all</b> stream. */
  def listToStream(
    streamName: String
  ): F[List[PersistentSubscriptionToStreamInfoResult[F]]]

  /** Lists all persistent subscriptions of a specific to the <b>\$all</b> stream. */
  def listToStream(
    streamName: String,
    options: ListPersistentSubscriptionsSettings
  ): F[List[PersistentSubscriptionToStreamInfoResult[F]]]

  /** Replays a persistent subscription to the <b>\$all</b> stream parked events. */
  def replayParkedMessagesToAll(subscriptionGroupName: String): F[Unit]

  /** Replays a persistent subscription to the <b>\$all</b> stream parked events. */
  def replayParkedMessagesToAll(
    subscriptionGroupName: String,
    options: ReplayParkedMessagesSettings
  ): F[Unit]

  /** Replays a persistent subscription to a stream parked events. */
  def replayParkedMessagesToStream(
    streamName: String,
    subscriptionGroupName: String
  ): F[Unit]

  /** Replays a persistent subscription to a stream parked events. */
  def replayParkedMessagesToStream(
    streamName: String,
    subscriptionGroupName: String,
    options: ReplayParkedMessagesSettings
  ): F[Unit]

  /** Restarts the server persistent subscription subsystem. */
  def restartSubsystem: F[Unit]

  /** Restarts the server persistent subscription subsystem. */
  def restartSubsystem(
    options: RestartPersistentSubscriptionSubsystemSettings
  ): F[Unit]

  /** Updates a persistent subscription group on the <b>all</b> stream. */
  def updateToAll(
    subscriptionGroupName: String
  ): F[Unit]

  /** Updates a persistent subscription group on the <b>\$all</b> stream. */
  def updateToAll(
    subscriptionGroupName: String,
    options: UpdatePersistentSubscriptionToAllSettings
  ): F[Unit]
}

object PersistentSession {

  /** Create a [[PersistentSession]]
    *
    * @param options
    *   The options to use when connecting to the EventStoreDB server
    * @tparam F
    *   The effect type
    * @return
    *   A [[PersistentSession]] as a Resource
    */
  def resource[F[_]: FutureLift: MonadCancelThrow](
    options: EventStoreSettings
  )(
    implicit
    file: File,
    line: Line,
    trace: Trace[F]
  ): Resource[F, PersistentSession[F]] =
    for {
      client  <- PersistentClientBuilder.resource[F](options)
      session <- PersistentSessionBuilder.fromClientResource[F](client)
    } yield session

  /** Create a [[PersistentSession]]
    *
    * @param options
    *   The options to use when connecting to the EventStoreDB server
    * @tparam F
    *   The effect type
    * @return
    *   A [[PersistentSession]] as a Stream
    */
  def stream[F[_]: FutureLift: MonadCancelThrow](
    options: EventStoreSettings
  )(
    implicit file: File,
    line: Line,
    trace: Trace[F]
  ): Stream[F, PersistentSession[F]] =
    for {
      client  <- PersistentClientBuilder.stream[F](options)
      session <- PersistentSessionBuilder.fromClientStream[F](client)
    } yield session
}
