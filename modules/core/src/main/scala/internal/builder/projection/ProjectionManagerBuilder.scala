// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.internal.builder.client

import scala.jdk.CollectionConverters.*
import dolphin.internal.syntax.all.*
import dolphin.internal.util.FutureLift
import dolphin.outcome.ProjectionDetails
import dolphin.setting.*
import dolphin.{ProjectionManager, Stateful, Trace}
import cats.Parallel
import cats.effect.Async
import cats.effect.kernel.Resource
import cats.syntax.applicative.*
import cats.syntax.flatMap.*
import cats.syntax.parallel.*
import com.eventstore.dbclient.EventStoreDBProjectionManagementClient
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.`type`.TypeFactory
import fs2.Stream
import sourcecode.{File, Line}

private[dolphin] object ProjectionManagerBuilder {

  def fromClientResource[F[_]: Async: FutureLift: Parallel](
    client: EventStoreDBProjectionManagementClient
  )(
    implicit line: Line,
    file: File,
    trace: Trace[F]
  ): Resource[F, ProjectionManager[F]] =
    Resource.make {
      FutureLift[F].delay(new ProjectionManager[F] { self =>
        def abort(projectionName: String): F[Unit] = self.abort(projectionName, AbortProjectionSettings.Default)

        def abort(projectionName: String, settings: AbortProjectionSettings): F[Unit] =
          FutureLift[F].futureLift(client.abort(projectionName, settings.toOptions)).withTrace

        def create(
          projectionName: String,
          query: String
        ): F[Unit] = self.create(projectionName, query, CreateProjectionSettings.Default)

        def create(projectionName: String, query: String, settings: CreateProjectionSettings): F[Unit] =
          FutureLift[F].futureLift(client.create(projectionName, query, settings.toOptions)).withTrace

        def delete(projectionName: String): F[Unit] = self.delete(projectionName, DeleteProjectionSettings.Default)

        def delete(projectionName: String, settings: DeleteProjectionSettings): F[Unit] =
          FutureLift[F].futureLift(client.delete(projectionName, settings.toOptions)).withTrace

        def disable(projectionName: String): F[Unit] = self.disable(projectionName, DisableProjectionSettings.Default)

        def disable(projectionName: String, settings: DisableProjectionSettings): F[Unit] =
          FutureLift[F]
            .futureLift(client.disable(projectionName, settings.toOptions))
            .withTrace

        def enable(projectionName: String): F[Unit] = self.enable(projectionName, EnableProjectionSettings.Default)

        def enable(projectionName: String, settings: EnableProjectionSettings): F[Unit] =
          FutureLift[F]
            .futureLift(client.enable(projectionName, settings.toOptions))
            .withTrace

        def getResult[T <: Stateful[?]](
          projectionName: String,
          `type`: Class[T]
        ): F[T] = self.getResult(projectionName, `type`, GetProjectionResultSettings.Default)

        def getResult[T <: Stateful[?]](
          projectionName: String,
          `type`: Class[T],
          settings: GetProjectionResultSettings
        ): F[T] =
          FutureLift[F]
            .futureLift(client.getResult(projectionName, `type`, settings.toOptions))
            .withTraceIdentity

        def getResult[T](
          projectionName: String,
          f: TypeFactory => JavaType
        ): F[T] = self.getResult(projectionName, f, GetProjectionResultSettings.Default)

        def getResult[T](
          projectionName: String,
          f: TypeFactory => JavaType,
          settings: GetProjectionResultSettings
        ): F[T] = FutureLift[F]
          .futureLift(client.getResult(projectionName, f(_), settings.toOptions))

        def getState[T <: Stateful[?]](
          projectionName: String,
          `type`: Class[T]
        ): F[T] = self.getState(projectionName, `type`, GetProjectionStateSettings.Default)

        def getState[T <: Stateful[?]](
          projectionName: String,
          `type`: Class[T],
          settings: GetProjectionStateSettings
        ): F[T] =
          FutureLift[F]
            .futureLift(client.getState(projectionName, `type`, settings.toOptions))
            .withTraceIdentity

        def getState[T](
          projectionName: String,
          f: TypeFactory => JavaType
        ): F[T] = self.getState(projectionName, f, GetProjectionStateSettings.Default)

        def getState[T](
          projectionName: String,
          f: TypeFactory => JavaType,
          settings: GetProjectionStateSettings
        ): F[T] = FutureLift[F]
          .futureLift(client.getState(projectionName, f(_), settings.toOptions))

        def getStatistics(
          projectionName: String
        ): F[ProjectionDetails] = self.getStatistics(projectionName, GetProjectionStatisticsSettings.Default)

        def getStatistics(projectionName: String, settings: GetProjectionStatisticsSettings): F[ProjectionDetails] =
          FutureLift[F]
            .futureLift(client.getStatistics(projectionName, settings.toOptions))
            .withTraceAndTransformer(ProjectionDetails.make)

        def isShutdown: Boolean = client.isShutdown

        def list: F[List[ProjectionDetails]] = self.list(ListProjectionsSettings.Default)

        def list(settings: ListProjectionsSettings): F[List[ProjectionDetails]] = FutureLift[F]
          .futureLift(client.list(settings.toOptions))
          .flatMap(_.asScala.toList.parTraverse(value => ProjectionDetails.make(value).pure[F]))

        def reset(projectionName: String): F[Unit] = self.reset(projectionName, ResetProjectionSettings.Default)

        def reset(projectionName: String, settings: ResetProjectionSettings): F[Unit] =
          FutureLift[F]
            .futureLift(client.reset(projectionName, settings.toOptions))
            .withTrace

        def restartSubsystem: F[Unit] = self.restartSubsystem(RestartProjectionSubsystemSettings.Default)

        def restartSubsystem(settings: RestartProjectionSubsystemSettings): F[Unit] =
          FutureLift[F]
            .futureLift(client.restartSubsystem(settings.toOptions))
            .withTrace

        def updateQuery(
          projectionName: String,
          query: String
        ): F[Unit] = self.updateQuery(projectionName, query, UpdateProjectionSettings.Default)

        def updateQuery(projectionName: String, query: String, settings: UpdateProjectionSettings): F[Unit] =
          FutureLift[F]
            .futureLift(client.update(projectionName, query, settings.toOptions))
            .withTrace

        def shutdown: F[Unit] = FutureLift[F].delay(client.shutdown()).withTrace
      })
    }(_.shutdown)

  def fromClientStream[F[_]: Async: FutureLift: Parallel](
    client: EventStoreDBProjectionManagementClient
  )(
    implicit line: Line,
    file: File,
    trace: Trace[F]
  ): Stream[F, ProjectionManager[F]] = Stream.resource(fromClientResource(client))

}
