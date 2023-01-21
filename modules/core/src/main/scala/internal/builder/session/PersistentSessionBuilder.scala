// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.internal.builder.session

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

import dolphin.PersistentSession
import dolphin.internal.syntax.result.*
import dolphin.internal.util.FutureLift
import dolphin.internal.util.FutureLift.*
import dolphin.outcome.*
import dolphin.setting.*
import dolphin.trace.Trace

import cats.effect.kernel.{MonadCancelThrow, Resource}
import cats.syntax.apply.*
import com.eventstore.dbclient.EventStoreDBPersistentSubscriptionsClient
import fs2.Stream
import sourcecode.{File, Line}

private[dolphin] object PersistentSessionBuilder {

  def fromClientResource[F[_]: FutureLift: MonadCancelThrow](
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

        // TODO: Implement the rest of the methods getInfoToAll, getInfoToStream, listToAll, listToStream, listAll, replayParkedMessagesToAll, replayParkedMessagesToStream, restartSubsystem, subscribeToAll, subscribeToStream, updateToAll, updateToStream

        /** Gets a specific persistent subscription info to the <b>\$all</b> stream. */
        def getInfoToAll(
          subscriptionGroupName: String
        ): F[Option[PersistentSubscriptionToInfoOutcome[
          F,
          PersistentSubscriptionToStatsOutcome[F],
          PersistentSubscriptionToSettingsOutcome[F]
        ]]] = FutureLift[F]
          .futureLift(
            client
              .getInfoToAll(subscriptionGroupName)
          )
          .withTraceAndTransformer(_.toScala.map(PersistentSubscriptionToInfoOutcome.makeAll(_)))

        /** Gets a specific persistent subscription info to the <b>\$all</b> stream. */
        def getInfoToAll(
          subscriptionGroupName: String,
          options: GetPersistentSubscriptionInfoSettings
        ): F[Option[PersistentSubscriptionToInfoOutcome[
          F,
          PersistentSubscriptionToStatsOutcome[F],
          PersistentSubscriptionToSettingsOutcome[F]
        ]]] = FutureLift[F]
          .futureLift(
            client
              .getInfoToAll(subscriptionGroupName, options.toOptions)
          )
          .withTraceAndTransformer(_.toScala.map(PersistentSubscriptionToInfoOutcome.makeAll(_)))

        /** Gets a specific persistent subscription info to a stream. */
        def getInfoToStream(
          streamName: String,
          subscriptionGroupName: String
        ): F[Option[PersistentSubscriptionToInfoOutcome[
          F,
          PersistentSubscriptionToStatsOutcome[F],
          PersistentSubscriptionToSettingsOutcome[F]
        ]]] = FutureLift[F]
          .futureLift(
            client
              .getInfoToStream(streamName, subscriptionGroupName)
          )
          .withTraceAndTransformer(
            _.toScala.map(PersistentSubscriptionToInfoOutcome.makeStream(_))
          )

        /** Gets a specific persistent subscription info to a stream. */
        def getInfoToStream(
          streamName: String,
          subscriptionGroupName: String,
          options: GetPersistentSubscriptionInfoSettings
        ): F[Option[PersistentSubscriptionToInfoOutcome[
          F,
          PersistentSubscriptionToStatsOutcome[F],
          PersistentSubscriptionToSettingsOutcome[F]
        ]]] = FutureLift[F]
          .futureLift(
            client
              .getInfoToStream(streamName, subscriptionGroupName, options.toOptions)
          )
          .withTraceAndTransformer(
            _.toScala.map(PersistentSubscriptionToInfoOutcome.makeStream(_))
          )

        /** Lists all existing persistent subscriptions. */
        def listAll: F[List[PersistentSubscriptionInfoOutcome[F]]] = FutureLift[F]
          .futureLift(
            client
              .listAll()
          )
          .withTraceAndTransformer(
            _.asScala.toList.map(PersistentSubscriptionInfoOutcome.make(_))
          )

        /** Lists all existing persistent subscriptions. */
        def listAll(
          options: ListPersistentSubscriptionsSettings
        ): F[List[PersistentSubscriptionInfoOutcome[F]]] = FutureLift[F]
          .futureLift(
            client
              .listAll(options.toOptions)
          )
          .withTraceAndTransformer(
            _.asScala.toList.map(PersistentSubscriptionInfoOutcome.make(_))
          )

        /** Lists all persistent subscriptions of a specific to the <b>\$all</b> stream. */
        def listToAll: F[List[PersistentSubscriptionToInfoOutcome[
          F,
          PersistentSubscriptionToStatsOutcome[F],
          PersistentSubscriptionToSettingsOutcome[F]
        ]]] = FutureLift[F]
          .futureLift(
            client
              .listToAll()
          )
          .withTraceAndTransformer(
            _.asScala.toList.map(PersistentSubscriptionToInfoOutcome.makeAll(_))
          )

        /** Lists all persistent subscriptions of a specific to the <b>\$all</b> stream. */
        def listToAll(
          options: ListPersistentSubscriptionsSettings
        ): F[List[PersistentSubscriptionToInfoOutcome[
          F,
          PersistentSubscriptionToStatsOutcome[F],
          PersistentSubscriptionToSettingsOutcome[F]
        ]]] = FutureLift[F]
          .futureLift(
            client
              .listToAll(options.toOptions)
          )
          .withTraceAndTransformer(
            _.asScala.toList.map(PersistentSubscriptionToInfoOutcome.makeAll(_))
          )

        /** Lists all persistent subscriptions of a specific to the <b>\$all</b> stream. */
        def listToStream(streamName: String): F[List[PersistentSubscriptionToInfoOutcome[
          F,
          PersistentSubscriptionToStatsOutcome[F],
          PersistentSubscriptionToSettingsOutcome[F]
        ]]] = FutureLift[F]
          .futureLift(
            client
              .listToStream(streamName)
          )
          .withTraceAndTransformer(
            _.asScala.toList.map(PersistentSubscriptionToInfoOutcome.makeStream(_))
          )

        /** Lists all persistent subscriptions of a specific to the <b>\$all</b> stream. */
        def listToStream(
          streamName: String,
          options: ListPersistentSubscriptionsSettings
        ): F[List[PersistentSubscriptionToInfoOutcome[
          F,
          PersistentSubscriptionToStatsOutcome[F],
          PersistentSubscriptionToSettingsOutcome[F]
        ]]] = FutureLift[F]
          .futureLift(
            client
              .listToStream(streamName, options.toOptions)
          )
          .withTraceAndTransformer(
            _.asScala.toList.map(PersistentSubscriptionToInfoOutcome.makeStream(_))
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
      })
    }(_.shutdown)

  def fromClientStream[F[_]: FutureLift: MonadCancelThrow](
    client: EventStoreDBPersistentSubscriptionsClient
  )(
    implicit line: Line,
    file: File,
    trace: Trace[F]
  ): Stream[F, PersistentSession[F]] = Stream.resource(fromClientResource(client))

}
