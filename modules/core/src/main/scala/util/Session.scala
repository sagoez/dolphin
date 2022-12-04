package dolphin.util

import java.util.UUID

import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success}

import dolphin.StoreSession
import dolphin.event.{WriteResult, ReadResult}
import dolphin.event.WriteResult.WriteResultOps
import dolphin.event.ReadResult.ReadResultOps
import dolphin.option.{ReadOptions, WriteOptions}

import cats.effect.kernel.{Async, Resource}
import cats.syntax.all.*
import com.eventstore.dbclient.{WriteResult as _, ReadResult as _, *}
import org.typelevel.log4cats.Logger
import cats.Applicative
import cats.FlatMap
import fs2.Stream

// TODO: Write pretty printer for the errors so users can see what went wrong in a very VERY easy way
// TODO: Add metadata to the write methods
private[dolphin] object Session {

  private def eventData[F[_]: Applicative: FlatMap](
    event: Array[Byte],
    `type`: String,
  ) = UUID.randomUUID().pure[F].flatMap { uuid =>
    EventData
      .builderAsBinary(`type`, event)
      .eventId(uuid)
      .build()
      .pure[F]
  }

  private def eventData[F[_]: Applicative: FlatMap](
    events: List[List[Byte]],
    `type`: String,
  ) = UUID.randomUUID().pure[F].flatMap { uuid =>
    events
      .map(event => EventData.builderAsBinary(uuid, `type`, event.toArray).build())
      .asJava
      .iterator()
      .pure[F]
  }

  def fromClientResource[F[_]: Async: Logger](client: EventStoreDBClient): Resource[F, StoreSession[F]] =
    Resource.make {
      new StoreSession[F] {
        def shutdown: F[Unit] = Async[F].delay(client.shutdown())

        def write(
          stream: String,
          event: Array[Byte],
          `type`: String,
        ): F[WriteResult[F]] = eventData(event, `type`).flatMap { event =>
          client
            .appendToStream(stream, event)
            .toSafeAttempt

        }
        def write(
          stream: String,
          options: WriteOptions,
          event: Array[Byte],
          `type`: String,
        ): F[WriteResult[F]] =
          options.get match {
            case Failure(exception) =>
              Logger[F].error(exception)(s"Failed to get options: $exception") *> Async[F].raiseError(exception)
            case Success(options)   =>
              eventData(event, `type`).flatMap { events =>
                client.appendToStream(stream, options, events).toSafeAttempt
              }

          }

        def write(
          stream: String,
          options: WriteOptions,
          events: List[List[Byte]],
          `type`: String,
        ): F[WriteResult[F]] =
          options.get match {
            case Failure(exception) =>
              Logger[F].error(exception)(s"Failed to get options: $exception") *> Async[F].raiseError(exception)
            case Success(options)   =>
              eventData(events, `type`).flatMap { events =>
                client.appendToStream(stream, options, events).toSafeAttempt
              }

          }

        def write(
          stream: String,
          events: List[List[Byte]],
          `type`: String,
        ): F[WriteResult[F]] = eventData(events, `type`).flatMap { events =>
          client
            .appendToStream(
              stream,
              events,
            )
            .toSafeAttempt

        }

        def read(
          stream: String,
          options: ReadOptions,
        ): F[ReadResult[F]] =
          options.get match {
            case Failure(exception) =>
              Logger[F].error(exception)(s"Failed to get read options: $exception") >> Async[F].raiseError(exception)
            case Success(options)   => client.readStream(stream, options).toSafeAttempt
          }

      }.pure[F]
    }(_.shutdown)

  def fromClientStream[F[_]: Async: Logger](
    client: EventStoreDBClient
  ): Stream[F, StoreSession[F]] = Stream.resource(fromClientResource(client))
}
