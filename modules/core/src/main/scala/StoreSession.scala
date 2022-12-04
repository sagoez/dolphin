package dolphin

import dolphin.event.{WriteResult, ReadResult}
import dolphin.option.{ReadOptions, WriteOptions}
import dolphin.util.{Client, Session}

import cats.effect.kernel.{Async, Resource}
import org.typelevel.log4cats.Logger
import fs2.Stream

trait StoreSession[F[_]] { self =>

  /** Write an event to a stream
    * @param stream
    *   The stream to write to
    * @param event
    *   The event to write as a string
    * @param `type`
    *   The type of the event, used for filtering and projections
    * @return
    *   A WriteResult containing the result of the write. Could fail if it fails to write the stream
    */
  def write(stream: String, event: Array[Byte], `type`: String): F[WriteResult[F]]

  /** Write an event to a stream
    * @param stream
    *   The stream to write to
    * @param options
    *   The options to use when writing to the stream
    * @param event
    *   The event to write as a string
    * @param `type`
    *   The type of the event, used for filtering and projections
    * @return
    *   A WriteResult containing the result of the write. Could fail if it fails to write the stream
    */
  def write(stream: String, options: WriteOptions, event: Array[Byte], `type`: String): F[WriteResult[F]]

  /** Write a list of events to a stream
    * @param stream
    *   The stream to write to
    * @param options
    *   The options to use when writing to the stream
    * @param events
    *   The events to write as a list of strings
    * @param `type`
    *   The type of the event, used for filtering and projections
    * @return
    *   A WriteResult containing the result of the write. Could fail if it fails to write the stream
    */
  def write(stream: String, options: WriteOptions, events: List[List[Byte]], `type`: String): F[WriteResult[F]]

  /** Write a list of events to a stream
    * @param stream
    *   The stream to write to
    * @param events
    *   The events to write as a list of strings
    * @param `type`
    *   The type of the event, used for filtering and projections
    * @return
    *   A WriteResult containing the result of the write. Could fail if it fails to write the stream
    */
  def write(stream: String, events: List[List[Byte]], `type`: String): F[WriteResult[F]]

  /** Read events from a stream.
    * @param stream
    *   The stream to read from.
    * @param options
    *   The options to use when reading from the stream.
    * @return
    *   A ReadResult containing the result of the read. Could fail if the stream does not exist.
    */

  def read(
    stream: String,
    options: ReadOptions,
  ): F[ReadResult[F]]

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
    tls: Boolean,
  ): Resource[F, StoreSession[F]] =
    for {
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
    tls: Boolean,
  ): Stream[F, StoreSession[F]] =
    for {
      client  <- Client.makeStream[F](host, port, tls)
      session <- Session.fromClientStream[F](client)
    } yield session
}
