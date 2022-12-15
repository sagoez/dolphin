// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin

import dolphin.client.{Client, Session}
import dolphin.concurrent.SubscriptionListener.{WithFuture, WithStream}
import dolphin.event.{DeleteResult, ReadResult, WriteResult}
import dolphin.option.*
import dolphin.util.Trace

import cats.effect.kernel.{Async, Resource}
import fs2.Stream
import org.typelevel.log4cats.Logger
import sourcecode.{File, Line}

trait StoreSession[F[_]] { self =>

  /** Write an event to a stream
    * @param streamAggregateId
    *   The id that will aggregate all the events of the stream
    * @param event
    *   The event to write as a string
    * @param `type`
    *   The type of the event, used for filtering and projections
    * @return
    *   A WriteResult containing the result of the write. Could fail if it fails to write the stream
    */
  def write(streamAggregateId: String, event: EventWithMetadata, `type`: String): F[WriteResult[F]]

  /** Write an event to a stream
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
  def write(
    streamAggregateId: String,
    options: WriteOptions,
    event: EventWithMetadata,
    `type`: String
  ): F[WriteResult[F]]

  /** Write a list of events to a stream
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
  def write(
    streamAggregateId: String,
    options: WriteOptions,
    events: List[EventWithMetadata],
    `type`: String
  ): F[WriteResult[F]]

  /** Write a list of events to a stream
    * @param streamAggregateId
    *   The id that will aggregate all the events of the stream
    * @param events
    *   The events to write as a list of strings
    * @param `type`
    *   The type of the event, used for filtering and projections
    * @return
    *   A WriteResult containing the result of the write. Could fail if it fails to write the stream
    */
  def write(streamAggregateId: String, events: List[EventWithMetadata], `type`: String): F[WriteResult[F]]

  /** Read events from a stream.
    * @param streamAggregateId
    *   The id of the stream aggregate to read from
    * @param options
    *   The options to use when reading from the stream.
    * @return
    *   A ReadResult containing the result of the read. Could fail if the stream does not exist.
    */
  def read(
    streamAggregateId: String,
    options: ReadOptions
  ): F[ReadResult[F]]

  /** Listener used to handle catch-up subscription notifications raised throughout its lifecycle.
    * @param stream
    *   Aggregate id of the stream to subscribe to
    * @param options
    *   Options to use when subscribing to the stream
    * @return
    *   A Stream of Either a Throwable or an Event
    */
  def subscribeToStream(
    stream: String,
    listener: WithStream[F],
    options: SubscriptionOptions
  ): Stream[F, Either[Throwable, Event]]

  /** Listener used to handle catch-up subscription notifications raised throughout its lifecycle.
    *
    * @param stream
    *   Aggregate id of the stream to subscribe to
    * @param options
    *   Options to use when subscribing to the stream
    * @return
    *   A Stream of Either a Throwable or an Event
    */
  def subscribeToStream(
    stream: String,
    listener: WithFuture[F],
    options: SubscriptionOptions
  ): Stream[F, Unit]

  /** Listener used to handle catch-up subscription notifications raised throughout its lifecycle.
    *
    * @param stream
    *   Aggregate id of the stream to subscribe to
    * @return
    *   A Stream of Either a Throwable or an Event
    */
  def subscribeToStream(
    stream: String,
    listener: WithStream[F]
  ): Stream[F, Either[Throwable, Event]]

  /** Listener used to handle catch-up subscription notifications raised throughout its lifecycle.
    *
    * @param stream
    *   Aggregate id of the stream to subscribe to
    * @return
    *   A Stream of Either a Throwable or an Event
    */
  def subscribeToStream(
    stream: String,
    listener: WithFuture[F]
  ): Stream[F, Unit]

  /** Delete a stream from the EventStoreDB server
    * @param streamAggregateId
    *   The id of the stream aggregate to read from
    * @return
    *   A DeleteResult containing the result of the delete
    */
  def delete(streamAggregateId: String): F[DeleteResult[F]]

  /** Delete a stream from the EventStoreDB server
    * @param streamAggregateId
    *   The id of the stream aggregate to read from
    * @param options
    *   The options to use when deleting the stream
    * @return
    *   A DeleteResult containing the result of the delete
    */
  def delete(streamAggregateId: String, options: DeleteOptions): F[DeleteResult[F]]

  /** Tombstones a given stream.
    *
    * @param streamAggregateId
    *   The id of the stream aggregate to tombstone
    * @param options
    *   The options to use when deleting the stream
    * @return
    *   A DeleteResult containing the result of the delete
    */
  def tombstoneStream(streamAggregateId: String, options: DeleteOptions): F[DeleteResult[F]]

  /** Tombstones a given stream.
    *
    * @param streamAggregateId
    *   The id of the stream aggregate to tombstone
    * @return
    *   A DeleteResult containing the result of the delete
    */
  def tombstoneStream(streamAggregateId: String): F[DeleteResult[F]]
}

object StoreSession {

  /** Create a [[StoreSession]]
    * @param host
    *   The host to connect to the EventStoreDB server
    * @param port
    *   The port to connect to the EventStoreDB server
    * @param tls
    *   Whether to use TLS when connecting to the EventStoreDB server (default: false)
    * @tparam F
    *   The effect type
    * @return
    *   A [[StoreSession]] as a Resource
    */

  def resource[F[_]: Async: Logger](
    host: String,
    port: Int,
    tls: Boolean
  )(
    implicit line: Line,
    file: File
  ): Resource[F, StoreSession[F]] =
    for {
      case implicit0(trace: Trace[F]) <- Resource.pure(Trace.instance[F])
      client  <- Client.makeResource[F](host, port, tls)
      session <- Session.fromClientResource[F](client)
    } yield session

  /** Create a [[StoreSession]]
    * @param host
    *   The host to connect to the EventStoreDB server
    * @param port
    *   The port to connect to the EventStoreDB server
    * @param tls
    *   Whether to use TLS when connecting to the EventStoreDB server (default: false)
    * @tparam F
    *   The effect type
    * @return
    *   A [[StoreSession]] as a Stream
    */
  def stream[F[_]: Async: Logger](
    host: String,
    port: Int,
    tls: Boolean
  )(
    implicit line: Line,
    file: File
  ): Stream[F, StoreSession[F]] =
    for {
      case implicit0(trace: Trace[F]) <- Stream.emit(Trace.instance[F])
      client  <- Client.makeStream[F](host, port, tls)
      session <- Session.fromClientStream[F](client)
    } yield session
}
