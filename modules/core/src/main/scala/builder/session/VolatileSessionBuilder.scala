// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.builder.session

import java.util.UUID

import scala.jdk.CollectionConverters.*

import dolphin.concurrent.SubscriptionListener.{WithHandler, WithStream}
import dolphin.result.Result.*
import dolphin.setting.*
import dolphin.syntax.result.*
import dolphin.util.{FutureLift, Trace}
import dolphin.{Event, Metadata, VolatileSession}

import cats.effect.MonadCancelThrow
import cats.effect.kernel.Resource
import cats.syntax.all.*
import cats.{Applicative, FlatMap}
import com.eventstore.dbclient.{EventData, EventStoreDBClient}
import fs2.Stream
import sourcecode.{File, Line}

private[dolphin] object VolatileSessionBuilder {

  // TODO: Revisit uuid creation to give the client more freedom of handling idempotency, see https://developers.eventstore.com/clients/dotnet/21.2/appending.html#idempotence
  private def eventData[F[_]: Applicative: FlatMap](
    event: Event,
    metadata: Metadata,
    `type`: String
  ) = Applicative[F].pure(UUID.randomUUID()).flatMap { uuid =>
    Applicative[F].pure {
      EventData
        .builderAsBinary(uuid, `type`, event)
        .metadataAsBytes(metadata)
        .build()
    }
  }

  private def eventData[F[_]: Applicative: FlatMap](
    events: List[(Event, Metadata)],
    `type`: String
  ) = Applicative[F].pure(UUID.randomUUID()).flatMap { uuid =>
    Applicative[F].pure {
      events
        .map { event =>
          EventData
            .builderAsBinary(uuid, `type`, event._1)
            .metadataAsBytes(event._2)
            .build()
        }
        .asJava
        .iterator()
    }
  }

  def fromClientResource[F[_]: FutureLift: MonadCancelThrow](
    client: EventStoreDBClient
  )(
    implicit line: Line,
    file: File,
    trace: Trace[F]
  ): Resource[F, VolatileSession[F]] =
    Resource.make {
      FutureLift[F].delay(new VolatileSession[F] { self =>
        def shutdown: F[Unit] = FutureLift[F].delay(client.shutdown())

        def deleteStream(streamAggregateId: String): F[DeleteResult[F]] = FutureLift[F]
          .futureLift(
            client
              .deleteStream(streamAggregateId)
          )
          .withTraceAndTransformer(DeleteResult(_))

        def deleteStream(streamAggregateId: String, options: DeleteStreamSettings): F[DeleteResult[F]] = FutureLift[F]
          .futureLift(
            client
              .deleteStream(streamAggregateId, options.toOptions)
          )
          .withTraceAndTransformer(DeleteResult(_))

        def appendToStream(
          stream: String,
          event: Event,
          metadata: Metadata,
          `type`: String
        ): F[WriteResult[F]] = eventData(event, metadata, `type`).flatMap { event =>
          FutureLift[F]
            .futureLift(
              client
                .appendToStream(stream, event)
            )
            .withTraceAndTransformer(WriteResult(_))

        }

        def appendToStream(
          stream: String,
          options: AppendToStreamSettings,
          event: Event,
          metadata: Metadata,
          `type`: String
        ): F[WriteResult[F]] = eventData(event, metadata, `type`).flatMap { events =>
          FutureLift[F]
            .futureLift(client.appendToStream(stream, options.toOptions, events))
            .withTraceAndTransformer(WriteResult(_))
        }

        def appendToStream(
          stream: String,
          options: AppendToStreamSettings,
          events: List[(Event, Metadata)],
          `type`: String
        ): F[WriteResult[F]] = eventData(events, `type`).flatMap { events =>
          FutureLift[F]
            .futureLift(client.appendToStream(stream, options.toOptions, events))
            .withTraceAndTransformer(WriteResult(_))
        }

        def appendToStream(
          stream: String,
          events: List[(Event, Metadata)],
          `type`: String
        ): F[WriteResult[F]] = eventData(events, `type`).flatMap { events =>
          FutureLift[F]
            .futureLift(
              client
                .appendToStream(
                  stream,
                  events
                )
            )
            .withTraceAndTransformer(WriteResult(_))

        }

        def readStream(
          stream: String,
          options: ReadStreamSettings
        ): F[ReadResult[F]] = FutureLift[F]
          .futureLift(
            client
              .readStream(stream, options.toOptions)
          )
          .withTraceAndTransformer(ReadResult(_))

        def subscribeToStream(
          stream: String,
          listener: WithStream[F],
          options: SubscriptionToStreamSettings
        ): Stream[F, Either[Throwable, Event]] = Stream
          .eval(
            FutureLift[F]
              .futureLift(client.subscribeToStream(stream, listener.listener, options.toOptions))
          )
          .flatMap { _ =>
            listener.stream
          }

        def subscribeToStream(
          stream: String,
          listener: WithHandler[F],
          options: SubscriptionToStreamSettings
        ): Stream[F, Unit] =
          Stream
            .eval(
              FutureLift[F]
                .futureLift(client.subscribeToStream(stream, listener.listener, options.toOptions))
            )
            .void

        def subscribeToStream(
          stream: String,
          listener: WithStream[F]
        ): Stream[F, Either[Throwable, Event]] = Stream
          .eval(
            FutureLift[F]
              .futureLift(client.subscribeToStream(stream, listener.listener))
          )
          .flatMap(_ => listener.stream)

        def subscribeToStream(
          stream: String,
          listener: WithHandler[F]
        ): Stream[F, Unit] =
          Stream
            .eval(
              FutureLift[F]
                .futureLift(client.subscribeToStream(stream, listener.listener))
            )
            .void

        def tombstoneStream(streamAggregateId: String, options: DeleteStreamSettings): F[DeleteResult[F]] =
          FutureLift[F]
            .futureLift(
              client
                .tombstoneStream(streamAggregateId, options.toOptions)
            )
            .withTraceAndTransformer(DeleteResult(_))

        def tombstoneStream(streamAggregateId: String): F[DeleteResult[F]] =
          // Workaround while https://github.com/EventStore/EventStoreDB-Client-Java/issues/201 is not fixed
          self.tombstoneStream(streamAggregateId, DeleteStreamSettings.Default)
      })
    }(_.shutdown)

  def fromClientStream[F[_]: FutureLift: MonadCancelThrow](
    client: EventStoreDBClient
  )(
    implicit line: Line,
    file: File,
    trace: Trace[F]
  ): Stream[F, VolatileSession[F]] = Stream.resource(fromClientResource(client))
}
