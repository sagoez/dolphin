// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.outcome

import com.eventstore.dbclient

sealed trait ProjectionDetails {

  /** The projection's buffered events. */
  def getBufferedEvents: Long

  /** The projection's checkpoint status. */
  def getCheckpointStatus: String

  /** The core processing time.. */
  def getCoreProcessingTime: Long

  /** The projection's effective name. */
  def getEffectiveName: String

  /** The projection's current epoch. */
  def getEpoch: Long

  /** The projection's events processed after restart. */
  def getEventsProcessedAfterRestart: Long

  /** The projection's last checkpoint. */
  def getLastCheckpoint: String

  /** The projection's mode. */
  def getMode: String

  /** The projection's name. */
  def getName: String

  /** The number of partitions cached. */
  def getPartitionsCached: Int

  /** The projection's position. */
  def getPosition: String

  /** The projection's progress. */
  def getProgress: Float

  /** The projection's reads-in-progress. */
  def getReadsInProgress: Int

  /** The projection's StateReason. */
  def getStateReason: String

  /** The projection's status. */
  def getStatus: String

  /** The projection version. */
  def getVersion: Long

  /** The projection write pending events after checkpoint. */
  def getWritePendingEventsAfterCheckpoint: Int

  /** The projection write pending events before checkpoint. */
  def getWritePendingEventsBeforeCheckpoint: Int

  /** The projection's writes-in-progress. */
  def getWritesInProgress: Int
}

object ProjectionDetails {

  private[dolphin] def make(
    ctx: dbclient.ProjectionDetails
  ) =
    new ProjectionDetails {
      def getBufferedEvents: Long = ctx.getBufferedEvents

      def getCheckpointStatus: String = ctx.getCheckpointStatus

      def getCoreProcessingTime: Long = ctx.getCoreProcessingTime

      def getEffectiveName: String = ctx.getEffectiveName

      def getEpoch: Long = ctx.getEpoch

      def getEventsProcessedAfterRestart: Long = ctx.getEventsProcessedAfterRestart

      def getLastCheckpoint: String = ctx.getLastCheckpoint

      def getMode: String = ctx.getMode

      def getName: String = ctx.getName

      def getPartitionsCached: Int = ctx.getPartitionsCached

      def getPosition: String = ctx.getPosition

      def getProgress: Float = ctx.getProgress

      def getReadsInProgress: Int = ctx.getReadsInProgress

      def getStateReason: String = ctx.getStateReason

      def getStatus: String = ctx.getStatus

      def getVersion: Long = ctx.getVersion

      def getWritePendingEventsAfterCheckpoint: Int = ctx.getWritePendingEventsAfterCheckpoint

      def getWritePendingEventsBeforeCheckpoint: Int = ctx.getWritePendingEventsBeforeCheckpoint

      def getWritesInProgress: Int = ctx.getWritesInProgress
    }
}
