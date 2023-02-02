// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.internal.builder.session

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

import dolphin.Message.PersistentMessage
import dolphin.internal.builder.listener.PersistentSubscriptionListenerBuilder.{
  WithFutureHandlerBuilder,
  WithStreamHandlerBuilder
}
import dolphin.internal.syntax.result.*
import dolphin.internal.util.FutureLift
import dolphin.internal.util.FutureLift.*
import dolphin.outcome.*
import dolphin.setting.*
import dolphin.{PersistentSession, Trace}

import cats.Parallel
import cats.effect.Async
import cats.effect.kernel.Resource
import cats.effect.std.Dispatcher
import cats.syntax.apply.*
import cats.syntax.functor.*
import com.eventstore.dbclient.EventStoreDBPersistentSubscriptionsClient
import fs2.Stream
import sourcecode.{File, Line}

private[dolphin] object PersistentSessionBuilder {

  def fromClientResource[F[_]: Async: FutureLift: Parallel](
    client: EventStoreDBPersistentSubscriptionsClient
  )(
    implicit line: Line,
    file: File,
    trace: Trace[F]
  ): Resource[F, PersistentSession[F]] =
    Resource.make {
      FutureLift[F].delay(new PersistentSession[F] { self =>
        def shutdown: F[Unit] =
          Trace[F].trace("Shutting down persistent client") *> FutureLift[F].delay(client.shutdown())

        def createToAll(
          subscriptionGroupName: String,
          options: CreatePersistentSubscriptionToAllSettings
        ): F[Unit] =
          FutureLift[F]
            .futureLift(
              client
                .createToAll(subscriptionGroupName, options.toOptions)
            )
            .withTrace

        def createToAll(subscriptionGroupName: String): F[Unit] =
          client
            .createToAll(subscriptionGroupName)
            .futureLift
            .withTrace

        def createToStream(streamName: String, subscriptionGroupName: String): F[Unit] =
          FutureLift[F]
            .futureLift(
              client
                .createToStream(streamName, subscriptionGroupName)
            )
            .withTrace

        def createToStream(
          streamName: String,
          subscriptionGroupName: String,
          options: CreatePersistentSubscriptionToStreamSettings
        ): F[Unit] =
          FutureLift[F]
            .futureLift(
              client
                .createToStream(streamName, subscriptionGroupName, options.toOptions)
            )
            .withTrace

        def deleteToAll(subscriptionGroupName: String): F[Unit] =
          FutureLift[F]
            .futureLift(
              client
                .deleteToAll(subscriptionGroupName)
            )
            .withTrace

        def deleteToAll(
          subscriptionGroupName: String,
          options: DeletePersistentSubscriptionSettings
        ): F[Unit] =
          FutureLift[F]
            .futureLift(
              client
                .deleteToAll(subscriptionGroupName, options.toOptions)
            )
            .withTrace

        def deleteToStream(streamName: String, subscriptionGroupName: String): F[Unit] =
          FutureLift[F]
            .futureLift(
              client
                .deleteToStream(streamName, subscriptionGroupName)
            )
            .withTrace

        def deleteToStream(
          streamName: String,
          subscriptionGroupName: String,
          options: DeletePersistentSubscriptionSettings
        ): F[Unit] =
          FutureLift[F]
            .futureLift(
              client
                .deleteToStream(streamName, subscriptionGroupName, options.toOptions)
            )
            .withTrace

        /** Gets a specific persistent subscription info to the <b>\$all</b> stream. */
        def getInfoToAll(
          subscriptionGroupName: String
        ): F[Option[FromAllInformation]] = FutureLift[F]
          .futureLift(
            client
              .getInfoToAll(subscriptionGroupName)
          )
          .withTraceAndTransformer(_.toScala.map(Information.makeAll(_)))

        /** Gets a specific persistent subscription info to the <b>\$all</b> stream. */
        def getInfoToAll(
          subscriptionGroupName: String,
          options: GetPersistentSubscriptionInfoSettings
        ): F[Option[FromAllInformation]] = FutureLift[F]
          .futureLift(
            client
              .getInfoToAll(subscriptionGroupName, options.toOptions)
          )
          .withTraceAndTransformer(_.toScala.map(Information.makeAll(_)))

        /** Gets a specific persistent subscription info to a stream. */
        def getInfoToStream(
          streamName: String,
          subscriptionGroupName: String
        ): F[Option[FromStreamInformation]] = FutureLift[F]
          .futureLift(
            client
              .getInfoToStream(streamName, subscriptionGroupName)
          )
          .withTraceAndTransformer(
            _.toScala.map(Information.makeStream(_))
          )

        /** Gets a specific persistent subscription info to a stream. */
        def getInfoToStream(
          streamName: String,
          subscriptionGroupName: String,
          options: GetPersistentSubscriptionInfoSettings
        ): F[Option[FromStreamInformation]] = FutureLift[F]
          .futureLift(
            client
              .getInfoToStream(streamName, subscriptionGroupName, options.toOptions)
          )
          .withTraceAndTransformer(
            _.toScala.map(Information.makeStream(_))
          )

        /** Lists all existing persistent subscriptions. */
        def listAll: F[List[PersistentSubscription]] = FutureLift[F]
          .futureLift(
            client
              .listAll()
          )
          .withTraceAndTransformer(
            _.asScala.toList.map(PersistentSubscription.make(_))
          )

        /** Lists all existing persistent subscriptions. */
        def listAll(
          options: ListPersistentSubscriptionsSettings
        ): F[List[PersistentSubscription]] = FutureLift[F]
          .futureLift(
            client
              .listAll(options.toOptions)
          )
          .withTraceAndTransformer(
            _.asScala.toList.map(PersistentSubscription.make(_))
          )

        /** Lists all persistent subscriptions of a specific to the <b>\$all</b> stream. */
        def listToAll: F[List[FromAllInformation]] = FutureLift[F]
          .futureLift(
            client
              .listToAll()
          )
          .withTraceAndTransformer(
            _.asScala.toList.map(Information.makeAll(_))
          )

        /** Lists all persistent subscriptions of a specific to the <b>\$all</b> stream. */
        def listToAll(
          options: ListPersistentSubscriptionsSettings
        ): F[List[FromAllInformation]] = FutureLift[F]
          .futureLift(
            client
              .listToAll(options.toOptions)
          )
          .withTraceAndTransformer(
            _.asScala.toList.map(Information.makeAll(_))
          )

        /** Lists all persistent subscriptions of a specific to the <b>\$all</b> stream. */
        def listToStream(streamName: String): F[List[FromStreamInformation]] = FutureLift[F]
          .futureLift(
            client
              .listToStream(streamName)
          )
          .withTraceAndTransformer(
            _.asScala.toList.map(Information.makeStream(_))
          )

        /** Lists all persistent subscriptions of a specific to the <b>\$all</b> stream. */
        def listToStream(
          streamName: String,
          options: ListPersistentSubscriptionsSettings
        ): F[List[FromStreamInformation]] = FutureLift[F]
          .futureLift(
            client
              .listToStream(streamName, options.toOptions)
          )
          .withTraceAndTransformer(
            _.asScala.toList.map(Information.makeStream(_))
          )

        def replayParkedMessagesToAll(subscriptionGroupName: String): F[Unit] =
          FutureLift[F]
            .futureLift(
              client
                .replayParkedMessagesToAll(subscriptionGroupName)
            )
            .withTrace

        /** Replays a persistent subscription to the <b>\$all</b> stream parked events. */
        def replayParkedMessagesToAll(
          subscriptionGroupName: String,
          options: ReplayParkedMessagesSettings
        ): F[Unit] =
          FutureLift[F]
            .futureLift(
              client
                .replayParkedMessagesToAll(subscriptionGroupName, options.toOptions)
            )
            .withTrace

        /** Replays a persistent subscription to a stream parked events. */
        def replayParkedMessagesToStream(streamName: String, subscriptionGroupName: String): F[Unit] =
          FutureLift[F]
            .futureLift(
              client
                .replayParkedMessagesToStream(streamName, subscriptionGroupName)
            )
            .withTrace

        /** Replays a persistent subscription to a stream parked events. */
        def replayParkedMessagesToStream(
          streamName: String,
          subscriptionGroupName: String,
          options: ReplayParkedMessagesSettings
        ): F[Unit] =
          FutureLift[F]
            .futureLift(
              client
                .replayParkedMessagesToStream(streamName, subscriptionGroupName, options.toOptions)
            )
            .withTrace

        /** Restarts the server persistent subscription subsystem. */
        def restartSubsystem: F[Unit] =
          FutureLift[F]
            .futureLift(
              client
                .restartSubsystem()
            )
            .withTrace

        /** Restarts the server persistent subscription subsystem. */
        def restartSubsystem(options: RestartPersistentSubscriptionSubsystemSettings): F[Unit] =
          FutureLift[F]
            .futureLift(
              client
                .restartSubsystem(options.toOptions)
            )
            .withTrace

        /** Updates a persistent subscription group on the <b>\$all</b> stream. */
        def updateToAll(subscriptionGroupName: String): F[Unit] =
          FutureLift[F]
            .futureLift(
              client
                .updateToAll(subscriptionGroupName)
            )
            .withTrace

        /** Updates a persistent subscription group on the <b>\$all</b> stream. */
        def updateToAll(subscriptionGroupName: String, options: UpdatePersistentSubscriptionToAllSettings): F[Unit] =
          FutureLift[F]
            .futureLift(
              client
                .updateToAll(subscriptionGroupName, options.toOptions)
            )
            .withTrace

        /** Updates a persistent subscription group on a stream. */
        def updateToStream(streamName: String, subscriptionGroupName: String): F[Unit] =
          FutureLift[F]
            .futureLift(client.updateToStream(streamName, subscriptionGroupName))
            .withTrace

        /** Updates a persistent subscription group on a stream. */
        def updateToStream(
          streamName: String,
          subscriptionGroupName: String,
          options: UpdatePersistentSubscriptionToStreamSettings
        ): F[Unit] =
          FutureLift[F]
            .futureLift(client.updateToStream(streamName, subscriptionGroupName, options.toOptions))
            .withTrace

        def subscribeToStream(
          streamName: String,
          subscriptionGroupName: String
        ): Stream[F, PersistentMessage[F]] = self.subscribeToStream(
          streamName,
          subscriptionGroupName,
          PersistentSubscriptionSettings.Default
        )

        def subscribeToStream(
          streamName: String,
          subscriptionGroupName: String,
          options: PersistentSubscriptionSettings
        ): Stream[F, PersistentMessage[F]] =
          for {
            listener <- Stream.resource(WithStreamHandlerBuilder.make)
            stream   <- Stream
                          .eval(
                            FutureLift[F]
                              .futureLift(
                                client.subscribeToStream(
                                  streamName,
                                  subscriptionGroupName,
                                  options.toOptions,
                                  listener.listener
                                )
                              )
                          )
                          .flatMap(_ => listener.stream)
          } yield stream

        def subscribeToAll(
          subscriptionGroupName: String,
          options: PersistentSubscriptionSettings,
          handler: PersistentMessage[F] => F[Unit]
        ): Resource[F, Unit] =
          for {
            dispatcher   <- Dispatcher.sequential[F]
            subscription <-
              Resource
                .make(
                  FutureLift[F]
                    .futureLift(
                      client.subscribeToAll(
                        subscriptionGroupName,
                        options.toOptions,
                        WithFutureHandlerBuilder[F](handler, dispatcher).listener
                      )
                    )
                )(subscription => FutureLift[F].delay(subscription.stop()))
                .void
          } yield subscription

        def subscribeToAll(
          subscriptionGroupName: String,
          handler: PersistentMessage[F] => F[Unit]
        ): Resource[F, Unit] = self.subscribeToAll(
          subscriptionGroupName,
          PersistentSubscriptionSettings.Default,
          handler
        )

        def subscribeToAll(subscriptionGroupName: String): Stream[F, PersistentMessage[F]] = self.subscribeToAll(
          subscriptionGroupName,
          PersistentSubscriptionSettings.Default
        )

        def subscribeToAll(
          subscriptionGroupName: String,
          options: PersistentSubscriptionSettings
        ): Stream[F, PersistentMessage[F]] =
          for {
            listener <- Stream.resource(WithStreamHandlerBuilder.make)
            stream   <- Stream
                          .eval(
                            FutureLift[F]
                              .futureLift(
                                client.subscribeToAll(
                                  subscriptionGroupName,
                                  options.toOptions,
                                  listener.listener
                                )
                              )
                          )
                          .flatMap(_ => listener.stream)
          } yield stream

        def subscribeToStream(
          streamName: String,
          subscriptionGroupName: String,
          handler: PersistentMessage[F] => F[Unit]
        ): Resource[F, Unit] = self.subscribeToStream(
          streamName,
          subscriptionGroupName,
          PersistentSubscriptionSettings.Default,
          handler
        )

        def subscribeToStream(
          streamName: String,
          subscriptionGroupName: String,
          options: PersistentSubscriptionSettings,
          handler: PersistentMessage[F] => F[Unit]
        ): Resource[F, Unit] =
          for {
            dispatcher   <- Dispatcher.sequential[F]
            subscription <-
              Resource
                .make(
                  FutureLift[F]
                    .futureLift(
                      client.subscribeToStream(
                        streamName,
                        subscriptionGroupName,
                        options.toOptions,
                        WithFutureHandlerBuilder[F](handler, dispatcher).listener
                      )
                    )
                )(subscription => FutureLift[F].delay(subscription.stop()))
                .void
          } yield subscription
      })
    }(_.shutdown)

  def fromClientStream[F[_]: Async: FutureLift: Parallel](
    client: EventStoreDBPersistentSubscriptionsClient
  )(
    implicit line: Line,
    file: File,
    trace: Trace[F]
  ): Stream[F, PersistentSession[F]] = Stream.resource(fromClientResource(client))

}
