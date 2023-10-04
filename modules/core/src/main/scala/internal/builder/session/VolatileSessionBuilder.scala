// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.internal.builder.session

import java.util
import java.util.UUID

import scala.jdk.CollectionConverters.*

import dolphin.Message.VolatileMessage
import dolphin.internal.builder.listener.VolatileSubscriptionListenerBuilder.*
import dolphin.internal.syntax.result.*
import dolphin.internal.util.FutureLift
import dolphin.outcome.*
import dolphin.setting.*
import dolphin.{EventByte, MessageHandler, MetadataBye, Trace, VolatileSession}

import cats.effect.Async
import cats.effect.kernel.Resource
import cats.effect.std.Dispatcher
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import cats.syntax.parallel.*
import cats.{Applicative, FlatMap, Monad, Parallel}
import com.eventstore.dbclient.{EventData as JEventData, EventStoreDBClient}
import fs2.Stream
import sourcecode.{File, Line}

private[dolphin] object VolatileSessionBuilder {

  // TODO: Revisit uuid creation to give the
  //  client more freedom of handling idempotency,
  //  see https://developers.eventstore.com/clients/dotnet/21.2/appending.html#idempotence
  private def eventData[F[_]: Applicative: FlatMap](
    event: EventByte,
    metadata: MetadataBye,
    `type`: String
  ): F[JEventData] = Applicative[F].pure(UUID.randomUUID()).flatMap { uuid =>
    Applicative[F].pure {
      JEventData
        .builderAsBinary(uuid, `type`, event)
        .metadataAsBytes(metadata)
        .build()
    }
  }

  private def eventData[F[_]: Parallel: Monad](
    events: List[(EventByte, MetadataBye)],
    `type`: String
  ): F[util.Iterator[JEventData]] = events
    .parTraverse { case (event, metadata) => eventData(event, metadata, `type`) }
    .map(_.asJava.iterator())

  def fromClientResource[F[_]: Async: Parallel](
    client: EventStoreDBClient
  )(
    implicit line: Line,
    file: File,
    trace: Trace[F]
  ): Resource[F, VolatileSession[F]] =
    Resource.make {
      FutureLift[F].delay(new VolatileSession[F] { self =>
        def shutdown: F[Unit] = FutureLift[F].delay(client.shutdown())

        def deleteStream(
          streamAggregateId: String
        ): F[Delete] = self.deleteStream(streamAggregateId, DeleteStreamSettings.Default)

        def deleteStream(streamAggregateId: String, options: DeleteStreamSettings): F[Delete] = FutureLift[F]
          .futureLift(client.deleteStream(streamAggregateId, options.toOptions))
          .withTraceAndTransformer(Delete.make)

        def appendToStream(
          stream: String,
          event: EventByte,
          metadata: MetadataBye,
          `type`: String
        ): F[Write] = self.appendToStream(stream, AppendToStreamSettings.Default, event, metadata, `type`)

        def appendToStream(
          stream: String,
          options: AppendToStreamSettings,
          event: EventByte,
          metadata: MetadataBye,
          `type`: String
        ): F[Write] = eventData(event, metadata, `type`).flatMap { events =>
          FutureLift[F]
            .futureLift(client.appendToStream(stream, options.toOptions, events))
            .withTraceAndTransformer(Write.make)
        }

        def appendToStream(
          stream: String,
          options: AppendToStreamSettings,
          events: List[(EventByte, MetadataBye)],
          `type`: String
        ): F[Write] = eventData(events, `type`).flatMap { events =>
          FutureLift[F]
            .futureLift(client.appendToStream(stream, options.toOptions, events))
            .withTraceAndTransformer(Write.make)
        }

        def appendToStream(
          stream: String,
          events: List[(EventByte, MetadataBye)],
          `type`: String
        ): F[Write] = self.appendToStream(stream, AppendToStreamSettings.Default, events, `type`)

        def readStream(
          stream: String,
          options: ReadFromStreamSettings
        ): F[Read[F]] = FutureLift[F]
          .futureLift(client.readStream(stream, options.toOptions))
          .withTraceAndTransformer(Read.make)

        def tombstoneStream(streamAggregateId: String, options: DeleteStreamSettings): F[Delete] = FutureLift[F]
          .futureLift(client.tombstoneStream(streamAggregateId, options.toOptions))
          .withTraceAndTransformer(Delete.make)

        def tombstoneStream(streamAggregateId: String): F[Delete] = FutureLift[F]
          .futureLift(client.tombstoneStream(streamAggregateId))
          .withTraceAndTransformer(Delete.make)

        def subscribeToStream(
          streamAggregateId: String,
          handler: MessageHandler[F, VolatileMessage[F]]
        ): Resource[F, Unit] = self.subscribeToStream(streamAggregateId, SubscriptionToStreamSettings.Default, handler)

        def subscribeToStream(
          streamAggregateId: String,
          options: SubscriptionToStreamSettings,
          handler: MessageHandler[F, VolatileMessage[F]]
        ): Resource[F, Unit] =
          for {
            dispatcher   <- Dispatcher.sequential[F]
            subscription <-
              Resource
                .make(
                  FutureLift[F]
                    .futureLift(
                      client.subscribeToStream(
                        streamAggregateId,
                        WithFutureHandlerBuilder[F](handler, dispatcher).listener,
                        options.toOptions
                      )
                    )
                )(subscription => FutureLift[F].delay(subscription.stop()))
                .void
          } yield subscription

        def subscribeToStream(
          streamAggregateId: String
        ): Stream[F, VolatileMessage[F]] = self.subscribeToStream(
          streamAggregateId,
          SubscriptionToStreamSettings.Default
        )

        def subscribeToStream(
          streamAggregateId: String,
          options: SubscriptionToStreamSettings
        ): Stream[F, VolatileMessage[F]] =
          for {
            listener <- Stream.resource(WithStreamHandlerBuilder.make)
            stream   <- Stream
                          .eval(
                            FutureLift[F]
                              .futureLift(
                                client.subscribeToStream(
                                  streamAggregateId,
                                  listener.listener
                                )
                              )
                          )
                          .flatMap(_ => listener.stream)
          } yield stream

        /** If true, the connection is closed and all resources are released. */
        override def isShutdown: Boolean = client.isShutdown
      })
    }(_.shutdown)

  def fromClientStream[F[_]: Async: Parallel](
    client: EventStoreDBClient
  )(
    implicit line: Line,
    file: File,
    trace: Trace[F]
  ): Stream[F, VolatileSession[F]] = Stream.resource(fromClientResource(client))
}
