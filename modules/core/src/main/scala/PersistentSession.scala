// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin

import dolphin.Message.PersistentMessage
import dolphin.internal.builder.client.PersistentClientBuilder
import dolphin.internal.builder.session.PersistentSessionBuilder
import dolphin.outcome.*
import dolphin.setting.*

import cats.Parallel
import cats.effect.Async
import cats.effect.kernel.Resource
import fs2.Stream
import sourcecode.{File, Line}

/** The main entry point for the EventStoreDB client for <a
  * href="https://developers.eventstore.com/server/v22.10/persistent-subscriptions.html#persistent-subscription">persistent
  * subscriptions</a>.
  *
  * <br/> Represents EventStoreDB client for stream operations. A client instance maintains a two-way communication to
  * EventStoreDB. Many threads can use the EventStoreDB client simultaneously, or a single thread can make many
  * asynchronous requests.
  *
  * <br/> Persistent subscriptions are similar to catch-up subscriptions, but there are two key differences:
  *
  *   - The subscription checkpoint is maintained by the server. It means that when your client reconnects to the
  *     persistent subscription, it will automatically resume from the last known position.
  *
  *   - It's possible to connect more than one event consumer to the same persistent subscription. In that case, the
  *     server will load-balance the consumers, depending on the defined strategy, and distribute the events to them.
  *
  * <br/> Because of those, persistent subscriptions are defined as subscription groups that are defined and maintained
  * by the server. Consumer then connect to a particular subscription group, and the server starts sending event to the
  * consumer.
  *
  * <br/> You can read more about persistent subscriptions in the server <a
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
  ): F[Option[FromAllInformation]]

  /** Gets a specific persistent subscription info to the <b>\$all</b> stream. */
  def getInfoToAll(
    subscriptionGroupName: String,
    options: GetPersistentSubscriptionInfoSettings
  ): F[Option[FromAllInformation]]

  /** Gets a specific persistent subscription info to a stream. */
  def getInfoToStream(
    streamName: String,
    subscriptionGroupName: String
  ): F[Option[FromStreamInformation]]

  /** Gets a specific persistent subscription info to a stream. */
  def getInfoToStream(
    streamName: String,
    subscriptionGroupName: String,
    options: GetPersistentSubscriptionInfoSettings
  ): F[Option[FromStreamInformation]]

  /** Lists all existing persistent subscriptions. */
  def listAll: F[List[PersistentSubscription]]

  /** Lists all existing persistent subscriptions. */
  def listAll(
    options: ListPersistentSubscriptionsSettings
  ): F[List[PersistentSubscription]]

  /** Lists all persistent subscriptions of a specific to the <b>\$all</b> stream. */
  def listToAll: F[List[FromAllInformation]]

  /** Lists all persistent subscriptions of a specific to the <b>\$all</b> stream. */
  def listToAll(
    options: ListPersistentSubscriptionsSettings
  ): F[List[FromAllInformation]]

  /** Lists all persistent subscriptions of a specific to the <b>\$all</b> stream. */
  def listToStream(
    streamName: String
  ): F[List[FromStreamInformation]]

  /** Lists all persistent subscriptions of a specific to the <b>\$all</b> stream. */
  def listToStream(
    streamName: String,
    options: ListPersistentSubscriptionsSettings
  ): F[List[FromStreamInformation]]

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

  /** Updates a persistent subscription group on a stream. */
  def updateToStream(
    streamName: String,
    subscriptionGroupName: String
  ): F[Unit]

  /** Updates a persistent subscription group on a stream. */
  def updateToStream(
    streamName: String,
    subscriptionGroupName: String,
    options: UpdatePersistentSubscriptionToStreamSettings
  ): F[Unit]

  /** Subscribes to the <b>\$all</b> stream. */
  def subscribeToAll(
    subscriptionGroupName: String,
    handler: PersistentMessage[F] => F[Unit]
  ): Resource[F, Unit]

  /** Subscribes to the <b>\$all</b> stream. */
  def subscribeToAll(
    subscriptionGroupName: String,
    options: PersistentSubscriptionSettings,
    handler: PersistentMessage[F] => F[Unit]
  ): Resource[F, Unit]

  /** Subscribes to the <b>\$all</b> stream. */
  def subscribeToAll(
    subscriptionGroupName: String
  ): Stream[F, PersistentMessage[F]]

  /** Subscribes to the <b>\$all</b> stream. */
  def subscribeToAll(
    subscriptionGroupName: String,
    options: PersistentSubscriptionSettings
  ): Stream[F, PersistentMessage[F]]

  /** Subscribes to a stream. */
  def subscribeToStream(
    streamName: String,
    subscriptionGroupName: String
  ): Stream[F, PersistentMessage[F]]

  /** Subscribes to a stream. */
  def subscribeToStream(
    streamName: String,
    subscriptionGroupName: String,
    options: PersistentSubscriptionSettings
  ): Stream[F, PersistentMessage[F]]

  /** Subscribes to a stream. */
  def subscribeToStream(
    streamName: String,
    subscriptionGroupName: String,
    handler: PersistentMessage[F] => F[Unit]
  ): Resource[F, Unit]

  /** Subscribes to a stream. */
  def subscribeToStream(
    streamName: String,
    subscriptionGroupName: String,
    options: PersistentSubscriptionSettings,
    handler: PersistentMessage[F] => F[Unit]
  ): Resource[F, Unit]

  /** If true, the connection is closed and all resources are released. */
  def isShutdown: Boolean

  /** Closes the connection and releases all resources. */
  def shutdown: F[Unit]
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
  def resource[F[_]: Async: Parallel](
    options: Config
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
  def stream[F[_]: Async: Parallel](
    options: Config
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
