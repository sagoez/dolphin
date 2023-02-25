// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin

import dolphin.internal.builder.client.{ProjectionManagerBuilder, ProjectionManagerClientBuilder}
import dolphin.outcome.ProjectionDetails
import dolphin.setting.*

import cats.Parallel
import cats.effect.Async
import cats.effect.kernel.Resource
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.`type`.TypeFactory
import fs2.Stream
import sourcecode.{File, Line}

/** <a href="https://developers.eventstore.com/server/v22.10/projections.html#introduction">Projections</a> is an
  * EventStoreDB subsystem that lets you append new events or link existing events to streams in a reactive manner.
  *
  * <br/> Projections are good at solving one specific query type, a category known as <i>temporal correlation
  * queries</i>. This query type is common in business systems and few can execute these queries well. Projections
  * support the concept of continuous queries.
  *
  * <br/> When running a projection you can choose whether the query should run and give you all results present, or
  * whether the query should continue running into the future finding new results as they happen and updating its result
  * set.
  *
  * <br/> Keep in mind that all projections emit events as a reaction to events that they process. We call this effect
  * write amplification because emitting new events or link events creates additional load on the server IO.
  *
  * <br/> Streams where projections emit events cannot be used to append events from applications. When this happens,
  * the projection will detect events not produced by the projection itself and it will break.
  *
  * <br/> EventStoreDB ships with five built in projections:
  *   - By Category: <b>\$by_category</b>
  *   - By Event Type: <b>\$by_event_type</b>
  *   - By Correlation ID: <b>\$by_correlation_id</b>
  *   - Stream by Category: <b>\$stream_by_category</b>
  *   - Streams: <b>\$streams</b>
  *
  * <br/> You can find more information about these projections in the <a
  * href="https://developers.eventstore.com/clients/grpc/projections.html">official documentation</a>.
  */
trait ProjectionManager[F[_]] {

  /** Stops the projection without writing a checkpoint. This can be used to disable a projection that has been faulted.
    * @param projectionName
    *   The name of the projection to stop.
    */
  def abort(projectionName: String): F[Unit]

  /** Stops the projection without writing a checkpoint. This can be used to disable a projection that has been faulted.
    * @param projectionName
    *   The name of the projection to stop.
    * @param settings
    *   The settings to use for this operation.
    */
  def abort(projectionName: String, settings: AbortProjectionSettings): F[Unit]

  /** Creates a new projection in the stopped state. Enable needs to be called separately to start the projection.
    * @param projectionName
    *   The name of the projection to create.
    * @param query
    *   The query to create the projection with.
    */
  def create(projectionName: String, query: String): F[Unit]

  /** Creates a new projection in the stopped state. Enable needs to be called separately to start the projection.
    * @param projectionName
    *   The name of the projection to create.
    * @param query
    *   The query to create the projection with.
    * @param settings
    *   The settings to use for this operation.
    */
  def create(projectionName: String, query: String, settings: CreateProjectionSettings): F[Unit]

  /** Deletes a projection.
    * @param projectionName
    *   The name of the projection to delete.
    */
  def delete(projectionName: String): F[Unit]

  /** Deletes a projection.
    * @param projectionName
    *   The name of the projection to delete.
    * @param settings
    *   The settings to use for this operation.
    */
  def delete(projectionName: String, settings: DeleteProjectionSettings): F[Unit]

  /** Enables a projection.
    * @param projectionName
    *   The name of the projection to enable.
    */
  def disable(projectionName: String): F[Unit]

  /** Enables a projection.
    * @param projectionName
    *   The name of the projection to enable.
    * @param settings
    *   The settings to use for this operation.
    */
  def disable(projectionName: String, settings: DisableProjectionSettings): F[Unit]

  /** Enables a projection.
    * @param projectionName
    *   The name of the projection to disable.
    */
  def enable(projectionName: String): F[Unit]

  /** Enables a projection.
    * @param projectionName
    *   The name of the projection to disable.
    * @param settings
    *   The settings to use for this operation.
    */
  def enable(projectionName: String, settings: EnableProjectionSettings): F[Unit]

  /** Gets the projection's result.
    * @param projectionName
    *   The name of the projection to get the result for.
    * @param `type`
    *   Type of the class to construct for the result.
    * @tparam T
    *   The type of the result.
    */
  def getResult[T](projectionName: String, `type`: Class[T]): F[T]

  /** Gets the projection's result.
    * @param projectionName
    *   The name of the projection to get the result for.
    * @param `type`
    *   Type of the class to construct for the result.
    * @param settings
    *   The settings to use for this operation.
    * @tparam T
    *   The type of the result.
    */
  def getResult[T](
    projectionName: String,
    `type`: Class[T],
    settings: GetProjectionResultSettings
  ): F[T]

  /** Gets the projection's result.
    *
    * @param projectionName
    *   The name of the projection to get the result for.
    * @param f
    *   A function that takes a <a
    *   href="https://fasterxml.github.io/jackson-databind/javadoc/2.7/com/fasterxml/jackson/databind/type/TypeFactory.html">TypeFactory</a>
    *   and returns a <a
    *   href="https://fasterxml.github.io/jackson-databind/javadoc/2.7/com/fasterxml/jackson/databind/JavaType.html">JavaType<a>.
    * @tparam T
    *   The type of the result.
    */
  def getResult[T](
    projectionName: String,
    f: TypeFactory => JavaType
  ): F[T]

  /** Gets the projection's result.
    *
    * @param projectionName
    *   The name of the projection to get the result for.
    * @param f
    *   A function that takes a <a
    *   href="https://fasterxml.github.io/jackson-databind/javadoc/2.7/com/fasterxml/jackson/databind/type/TypeFactory.html">TypeFactory</a>
    *   and returns a <a
    *   href="https://fasterxml.github.io/jackson-databind/javadoc/2.7/com/fasterxml/jackson/databind/JavaType.html">JavaType<a>.
    * @param settings
    *   The settings to use for this operation.
    * @tparam T
    *   The type of the result.
    */
  def getResult[T](
    projectionName: String,
    f: TypeFactory => JavaType,
    settings: GetProjectionResultSettings
  ): F[T]

  /** Gets the state of the projection.
    * @param projectionName
    *   The name of the projection to get the state for.
    * @param `type`
    *   Type of the class to construct for the result.
    * @tparam T
    *   The type of the state.
    */
  def getState[T](projectionName: String, `type`: Class[T]): F[T]

  /** Gets the state of the projection.
    * @param projectionName
    *   The name of the projection to get the state for.
    * @param `type`
    *   Type of the class to construct for the result.
    * @param settings
    *   The settings to use for this operation.
    * @tparam T
    *   The type of the state.
    */
  def getState[T](
    projectionName: String,
    `type`: Class[T],
    settings: GetProjectionStateSettings
  ): F[T]

  /** Gets the state of the projection.
    *
    * @param projectionName
    *   The name of the projection to get the state for.
    * @param f
    *   A function that takes a <a
    *   href="https://fasterxml.github.io/jackson-databind/javadoc/2.7/com/fasterxml/jackson/databind/type/TypeFactory.html">TypeFactory</a>
    *   and returns a <a
    *   href="https://fasterxml.github.io/jackson-databind/javadoc/2.7/com/fasterxml/jackson/databind/JavaType.html">JavaType<a>.
    * @tparam T
    *   The type of the state.
    */
  def getState[T](
    projectionName: String,
    f: TypeFactory => JavaType
  ): F[T]

  /** Gets the state of the projection.
    *
    * @param projectionName
    *   The name of the projection to get the state for.
    * @param f
    *   A function that takes a <a
    *   href="https://fasterxml.github.io/jackson-databind/javadoc/2.7/com/fasterxml/jackson/databind/type/TypeFactory.html">TypeFactory</a>
    *   and returns a <a
    *   href="https://fasterxml.github.io/jackson-databind/javadoc/2.7/com/fasterxml/jackson/databind/JavaType.html">JavaType<a>.
    * @param settings
    *   The settings to use for this operation.
    * @tparam T
    *   The type of the state.
    */
  def getState[T](
    projectionName: String,
    f: TypeFactory => JavaType,
    settings: GetProjectionStateSettings
  ): F[T]

  /** Gets the statistics for a projection.
    * @param projectionName
    *   The name of the projection to get the statistics for.
    */
  def getStatistics(projectionName: String): F[ProjectionDetails]

  /** Gets the statistics for a projection.
    * @param projectionName
    *   The name of the projection to get the statistics for.
    * @param settings
    *   The settings to use for this operation.
    */
  def getStatistics(projectionName: String, settings: GetProjectionStatisticsSettings): F[ProjectionDetails]

  /** Checks if this client instance has been shutdown. */
  def isShutdown: Boolean

  /** Lists all continuous projections. */
  def list: F[List[ProjectionDetails]]

  /** Lists all continuous projections.
    * @param settings
    *   The settings to use for this operation.
    */
  def list(settings: ListProjectionsSettings): F[List[ProjectionDetails]]

  /** Resets a projection.
    * @param projectionName
    *   The name of the projection to reset.
    */
  def reset(projectionName: String): F[Unit]

  /** Resets a projection.
    * @param projectionName
    *   The name of the projection to reset.
    * @param settings
    *   The settings to use for this operation.
    */
  def reset(projectionName: String, settings: ResetProjectionSettings): F[Unit]

  /** Restarts the projection subsystem. */
  def restartSubsystem: F[Unit]

  /** Restarts the projection subsystem.
    * @param settings
    *   The settings to use for this operation.
    */
  def restartSubsystem(settings: RestartProjectionSubsystemSettings): F[Unit]

  /** Updates the projection's query and emit options.
    * @param projectionName
    *   The name of the projection to update.
    * @param query
    *   The query to use for the projection.
    */
  def updateQuery(projectionName: String, query: String): F[Unit]

  /** Updates the projection's query and emit options.
    * @param projectionName
    *   The name of the projection to update.
    * @param query
    *   The query to use for the projection.
    * @param settings
    *   The settings to use for this operation.
    */
  def updateQuery(projectionName: String, query: String, settings: UpdateProjectionSettings): F[Unit]

  /** Closes the connection and releases all resources. */
  def shutdown: F[Unit]
}

object ProjectionManager {

  /** Create a [[ProjectionManager]]
    *
    * @param options
    *   The options to use when connecting to the EventStoreDB server
    * @tparam F
    *   The effect type
    * @return
    *   A [[ProjectionManager]] as a Resource
    */
  def resource[F[_]: Async: Parallel](
    options: Config
  )(
    implicit file: File,
    line: Line,
    trace: Trace[F]
  ): Resource[F, ProjectionManager[F]] =
    for {
      client     <- ProjectionManagerClientBuilder.resource[F](options)
      projection <- ProjectionManagerBuilder.fromClientResource[F](client)
    } yield projection

  /** Create a [[ProjectionManager]]
    * @param options
    *   The options to use when connecting to the EventStoreDB server
    * @tparam F
    *   The effect type
    * @return
    *   A [[ProjectionManager]] as a Resource
    */
  def stream[F[_]: Async: Parallel](
    options: Config
  )(
    implicit file: File,
    line: Line,
    trace: Trace[F]
  ): Stream[F, ProjectionManager[F]] =
    for {
      client     <- ProjectionManagerClientBuilder.stream[F](options)
      projection <- ProjectionManagerBuilder.fromClientStream[F](client)
    } yield projection
}
