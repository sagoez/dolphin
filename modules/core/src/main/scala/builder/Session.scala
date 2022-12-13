// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.client

import java.util.UUID

import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success}

import dolphin.concurrent.SubscriptionListener
import dolphin.event.DeleteResult.DeleteResultOps
import dolphin.event.ReadResult.ReadResultOps
import dolphin.event.WriteResult.WriteResultOps
import dolphin.event.{DeleteResult, ReadResult, WriteResult}
import dolphin.option.*
import dolphin.util.Trace
import dolphin.{Event, EventWithMetadata, StoreSession}

import cats.effect.kernel.{Async, Resource}
import cats.syntax.all.*
import cats.{Applicative, FlatMap}
import com.eventstore.dbclient.{ReadResult as _, SubscriptionListener as _, WriteResult as _, *}
import fs2.Stream
import org.typelevel.log4cats.Logger

// TODO: Write pretty printer for the errors so users can see what went wrong in a very VERY easy way
private[dolphin] object Session {

  private def eventData[F[_]: Applicative: FlatMap](
    event: EventWithMetadata,
    `type`: String
  ) = UUID.randomUUID().pure[F].flatMap { uuid =>
    EventData
      .builderAsBinary(uuid, `type`, event._1)
      .metadataAsBytes(event._2.getOrElse(Array.emptyByteArray))
      .build()
      .pure[F]
  }

  private def eventData[F[_]: Applicative: FlatMap](
    events: List[EventWithMetadata],
    `type`: String
  ) = UUID.randomUUID().pure[F].flatMap { uuid =>
    events
      .map { event =>
        EventData
          .builderAsBinary(uuid, `type`, event._1)
          .metadataAsBytes(event._2.getOrElse(Array.emptyByteArray))
          .build()
      }
      .asJava
      .iterator()
      .pure[F]
  }

  def fromClientResource[F[_]: Async: Logger: Trace](
    client: EventStoreDBClient
  ): Resource[F, StoreSession[F]] =
    Resource.make {
      new StoreSession[F] {
        def shutdown: F[Unit] = Async[F].delay(client.shutdown())

        def delete(streamAggregateId: String): F[DeleteResult[F]] =
          client
            .deleteStream(streamAggregateId)
            .toSafeAttempt

        def delete(streamAggregateId: String, options: DeleteOptions): F[DeleteResult[F]] =
          options.get match {
            case Failure(exception) =>
              Trace[F].error(exception, Some("Failed to get delete options")) *> Async[F].raiseError(exception)
            case Success(options)   =>
              client
                .deleteStream(streamAggregateId, options)
                .toSafeAttempt
          }

        def write(
          stream: String,
          event: EventWithMetadata,
          `type`: String
        ): F[WriteResult[F]] = eventData(event, `type`).flatMap { event =>
          client
            .appendToStream(stream, event)
            .toSafeAttempt

        }

        def write(
          stream: String,
          options: WriteOptions,
          event: EventWithMetadata,
          `type`: String
        ): F[WriteResult[F]] =
          options.get match {
            case Failure(exception) =>
              Trace[F].error(exception, Some("Failed to get write options")) *> Async[F].raiseError(exception)
            case Success(options)   =>
              eventData(event, `type`).flatMap { events =>
                client.appendToStream(stream, options, events).toSafeAttempt
              }

          }

        def write(
          stream: String,
          options: WriteOptions,
          events: List[EventWithMetadata],
          `type`: String
        ): F[WriteResult[F]] =
          options.get match {
            case Failure(exception) =>
              Trace[F].error(exception, Some("Failed to get write options")) *> Async[F].raiseError(exception)
            case Success(options)   =>
              eventData(events, `type`).flatMap { events =>
                client.appendToStream(stream, options, events).toSafeAttempt
              }

          }

        def write(
          stream: String,
          events: List[EventWithMetadata],
          `type`: String
        ): F[WriteResult[F]] = eventData(events, `type`).flatMap { events =>
          client
            .appendToStream(
              stream,
              events
            )
            .toSafeAttempt

        }

        def read(
          stream: String,
          options: ReadOptions
        ): F[ReadResult[F]] =
          options.get match {
            case Failure(exception) =>
              Trace[F].error(exception, Some("Failed to get read options")) *> Async[F].raiseError(exception)
            case Success(options)   => client.readStream(stream, options).toSafeAttempt
          }

        // TODO: Add the rest of the methods and their corresponding data types

        def subscribeToStream(
          stream: String,
          options: SubscriptionOptions
        ): Stream[F, Either[Throwable, Event]] =
          options.get match {
            case Failure(exception) =>
              Stream.eval(
                Trace[F].error(exception, Some("Failed to get subscription options"))
              ) >> Stream
                .raiseError(
                  exception
                )

            case Success(options) =>
              val listener = SubscriptionListener.default[F]
              Stream
                .eval(
                  Async[F].fromCompletableFuture(client.subscribeToStream(stream, listener.underlying, options).pure[F])
                )
                .flatMap { _ =>
                  listener.subscription
                }
          }

        def subscribeToStream(stream: String): Stream[F, Either[Throwable, Event]] = {
          val listener = SubscriptionListener.default[F]
          Stream
            .eval(
              Async[F].fromCompletableFuture(client.subscribeToStream(stream, listener.underlying).pure[F])
            )
            .flatMap { _ =>
              listener.subscription
            }
        }

        def tombstoneStream(streamAggregateId: String, options: DeleteOptions): F[DeleteResult[F]] =
          options.get match {
            case Failure(exception) =>
              Trace[F].error(exception, Some("Failed to get delete options")) *> Async[F].raiseError(exception)
            case Success(options)   =>
              client
                .tombstoneStream(streamAggregateId, options)
                .toSafeAttempt
          }

        def tombstoneStream(streamAggregateId: String): F[DeleteResult[F]] =
          client
            .tombstoneStream(streamAggregateId)
            .toSafeAttempt
      }.pure[F]
    }(_.shutdown)

  def fromClientStream[F[_]: Async: Logger: Trace](
    client: EventStoreDBClient
  ): Stream[F, StoreSession[F]] = Stream.resource(fromClientResource(client))
}
