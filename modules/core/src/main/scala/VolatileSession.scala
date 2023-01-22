// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin

import dolphin.concurrent.VolatileSubscriptionListener.{WithHandler, WithStreamHandler}
import dolphin.internal.builder.client.VolatileClientBuilder
import dolphin.internal.builder.session.VolatileSessionBuilder
import dolphin.internal.util.FutureLift
import dolphin.outcome.{DeleteOutcome, ReadOutcome, ResolvedEventOutcome, WriteOutcome}
import dolphin.setting.*
import dolphin.trace.Trace

import cats.effect.kernel.{MonadCancelThrow, Resource}
import fs2.Stream
import sourcecode.{File, Line}

/** The main entry point for the EventStoreDB client, operates with catchup subscriptions.
  *
  * Represents EventStoreDB client for stream operations. A client instance maintains a two-way communication to
  * EventStoreDB. Many threads can use the EventStoreDB client simultaneously, or a single thread can make many
  * asynchronous requests.
  *
  * Subscriptions are created by calling subscribeToStream or subscribeToAll. The returned Subscription object is
  * managed by the client.
  */
trait VolatileSession[F[_]] extends Serializable { self =>

  /** Appends events to a given stream.
    *
    * @param streamAggregateId
    *   The id that will aggregate all the events of the stream
    * @param event
    *   The event to write as a string
    * @param `type`
    *   The type of the event, used for filtering and projections
    * @return
    *   A WriteResult containing the result of the write. Could fail if it fails to write the stream
    */
  def appendToStream(
    streamAggregateId: String,
    event: Event,
    metadata: Metadata,
    `type`: String
  ): F[WriteOutcome[F]]

  /** Appends events to a given stream.
    *
    * @param streamAggregateId
    *   The id that will aggregate all the events of the stream
    * @param options
    *   The options to use when writing to the stream
    * @param event
    *   The event to write as a string
    * @param `type`
    *   The type of the event, used for filtering and projections
    * @return
    *   A WriteResult containing the result of the write. Could fail if it fails to write the stream
    */
  def appendToStream(
    streamAggregateId: String,
    options: AppendToStreamSettings,
    event: Event,
    metadata: Metadata,
    `type`: String
  ): F[WriteOutcome[F]]

  /** Appends events to a given stream.
    *
    * @param streamAggregateId
    *   The id that will aggregate all the events of the stream
    * @param options
    *   The options to use when writing to the stream
    * @param events
    *   The events to write as a list of strings
    * @param `type`
    *   The type of the event, used for filtering and projections
    * @return
    *   A WriteResult containing the result of the write. Could fail if it fails to write the stream
    */
  def appendToStream(
    streamAggregateId: String,
    options: AppendToStreamSettings,
    events: List[(Event, Metadata)],
    `type`: String
  ): F[WriteOutcome[F]]

  /** Appends events to a given stream.
    *
    * @param streamAggregateId
    *   The id that will aggregate all the events of the stream
    * @param events
    *   The events to write as a list of strings
    * @param `type`
    *   The type of the event, used for filtering and projections
    * @return
    *   A WriteResult containing the result of the write. Could fail if it fails to write the stream
    */
  def appendToStream(
    streamAggregateId: String,
    events: List[(Event, Metadata)],
    `type`: String
  ): F[WriteOutcome[F]]

  /** Read events from a stream. The reading can be done forwards and backwards.
    *
    * @param streamAggregateId
    *   The id of the stream aggregate to read from
    * @param options
    *   The options to use when reading from the stream.
    * @return
    *   A ReadResult containing the result of the read. Could fail if the stream does not exist.
    */
  def readStream(
    streamAggregateId: String,
    options: ReadFromStreamSettings
  ): F[ReadOutcome[F]]

  /** Listener used to handle catch-up subscription notifications raised throughout its lifecycle.
    *
    * Subscriptions allow you to subscribe to a stream and receive notifications about new events added to the stream.
    * appears. Dolphin will automatically handle the subscription lifecycle for you, and provide you with a stream of
    * Either a Throwable or an Event.
    *
    * @param streamAggregateId
    *   Aggregate id of the stream to subscribe to
    * @param options
    *   Options to use when subscribing to the stream
    * @return
    *   A Stream of Either a Throwable or an Event
    */
  def subscribeToStream(
    streamAggregateId: String,
    listener: WithStreamHandler[F],
    options: SubscriptionToStreamSettings
  ): Stream[F, Either[Throwable, ResolvedEventOutcome[F]]]

  /** Listener used to handle catch-up subscription notifications raised throughout its lifecycle.
    *
    * Subscriptions allow you to subscribe to a stream and receive notifications about new events added to the stream.
    * You provide an even handler and an optional starting point to the subscription. The handler is called for each
    * event from the starting point onward. If events already exist, the handler will be called for each event one by
    * one until it reaches the end of the stream. From there, the server will notify the handler whenever a new event
    * appears.
    *
    * @param streamAggregateId
    *   Aggregate id of the stream to subscribe to
    * @param options
    *   Options to use when subscribing to the stream
    * @return
    *   A Stream of Either a Throwable or an Event
    */
  def subscribeToStream(
    streamAggregateId: String,
    listener: WithHandler[F],
    options: SubscriptionToStreamSettings
  ): F[Unit]

  /** Listener used to handle catch-up subscription notifications raised throughout its lifecycle.
    *
    * Subscriptions allow you to subscribe to a stream and receive notifications about new events added to the stream.
    * appears. Dolphin will automatically handle the subscription lifecycle for you, and provide you with a stream of
    * Either a Throwable or an Event.
    *
    * @param streamAggregateId
    *   Aggregate id of the stream to subscribe to
    * @return
    *   A Stream of Either a Throwable or an Event
    */
  def subscribeToStream(
    streamAggregateId: String,
    handler: WithStreamHandler[F]
  ): Stream[F, Either[Throwable, ResolvedEventOutcome[F]]]

  /** Listener used to handle catch-up subscription notifications raised throughout its lifecycle.
    *
    * Subscriptions allow you to subscribe to a stream and receive notifications about new events added to the stream.
    * You provide an even handler and an optional starting point to the subscription. The handler is called for each
    * event from the starting point onward. If events already exist, the handler will be called for each event one by
    * one until it reaches the end of the stream. From there, the server will notify the handler whenever a new event
    * appears.
    *
    * @param streamAggregateId
    *   Aggregate id of the stream to subscribe to
    * @return
    *   A Stream of Either a Throwable or an Event
    */
  def subscribeToStream(
    streamAggregateId: String,
    handler: WithHandler[F]
  ): F[Unit]

  /** Makes use of Truncate before. When a stream is deleted, its Truncate before is set to the stream's current last
    * event number. When a deleted stream is read, the read will return a <i>StreamNotFound</i> error. After deleting
    * the stream, you are able to write to it again, continuing from where it left off.
    *
    * <i>Note: Deletion is reversible until the scavenging process runs.</i>
    *
    * @param streamAggregateId
    *   The id of the stream aggregate to read from
    * @return
    *   A DeleteResult containing the result of the delete
    */
  def deleteStream(streamAggregateId: String): F[DeleteOutcome[F]]

  /** Makes use of Truncate before. When a stream is deleted, its Truncate before is set to the stream's current last
    * event number. When a deleted stream is read, the read will return a <i>StreamNotFound</i> error. After deleting
    * the stream, you are able to write to it again, continuing from where it left off.
    *
    * <i>Note: Deletion is reversible until the scavenging process runs.</i>
    *
    * @param streamAggregateId
    *   The id of the stream aggregate to read from
    * @param options
    *   The options to use when deleting the stream
    * @return
    *   A DeleteResult containing the result of the delete
    */
  def deleteStream(streamAggregateId: String, options: DeleteStreamSettings): F[DeleteOutcome[F]]

  /** Writes a tombstone event to the stream, permanently deleting it. The stream cannot be recreated or written to
    * again. Tombstone events are written with the event's type <b>streamDeleted</b>. When a tombstoned stream is read,
    * the read will return a StreamDeleted error.
    *
    * @param streamAggregateId
    *   The id of the stream aggregate to tombstone
    * @param options
    *   The options to use when deleting the stream
    * @return
    *   A DeleteResult containing the result of the delete
    */
  def tombstoneStream(streamAggregateId: String, options: DeleteStreamSettings): F[DeleteOutcome[F]]

  /** Writes a tombstone event to the stream, permanently deleting it. The stream cannot be recreated or written to
    * again. Tombstone events are written with the event's type <b>streamDeleted</b>. When a tombstoned stream is read,
    * the read will return a StreamDeleted error.
    *
    * @param streamAggregateId
    *   The id of the stream aggregate to tombstone
    * @return
    *   A DeleteResult containing the result of the delete
    */
  def tombstoneStream(streamAggregateId: String): F[DeleteOutcome[F]]
}

object VolatileSession {

  /** Create a [[VolatileSession]]
    *
    * @param options
    *   The options to use when connecting to the EventStoreDB server
    * @tparam F
    *   The effect type
    * @return
    *   A [[VolatileSession]] as a Resource
    */
  def resource[F[_]: FutureLift: MonadCancelThrow](
    options: EventStoreSettings
  )(
    implicit file: File,
    line: Line,
    trace: Trace[F]
  ): Resource[F, VolatileSession[F]] =
    for {
      client  <- VolatileClientBuilder.resource[F](options)
      session <- VolatileSessionBuilder.fromClientResource[F](client)
    } yield session

  /** Create a [[VolatileSession]]
    *
    * @param options
    *   The options to use when connecting to the EventStoreDB server
    * @tparam F
    *   The effect type
    * @return
    *   A [[VolatileSession]] as a Stream
    */
  def stream[F[_]: FutureLift: MonadCancelThrow](
    options: EventStoreSettings
  )(
    implicit file: File,
    line: Line,
    trace: Trace[F]
  ): Stream[F, VolatileSession[F]] =
    for {
      client  <- VolatileClientBuilder.stream[F](options)
      session <- VolatileSessionBuilder.fromClientStream[F](client)
    } yield session
}
