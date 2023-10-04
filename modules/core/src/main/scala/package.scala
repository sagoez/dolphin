// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

import dolphin.concurrent.{ConsumerStrategy, NodePreference as DNodePreference}

package object dolphin {

  type EventByte   = Array[Byte]
  type MetadataBye = Array[Byte]

  type MessageHandler[F[_], T <: Message[F, ?]] = T => F[Unit]

  type CommitUnsigned  = Long
  type PrepareUnsigned = Long

  val Deadline: Long                  = 10_000
  val ReadFromEnd: Boolean            = false
  val ReadFromStart: Boolean          = true
  val DiscoveryInterval               = 500
  val GossipTimeout                   = 3000
  val KeepAliveTimeout: Long          = 10000
  // In case of persistent subscription you can set KeepAliveTimeout to -1 to disable it completely.
  val KeepAliveInterval: Long         = 10000
  val MaxDiscoverAttempts             = 3
  val NodePreference: DNodePreference = DNodePreference.Leader

  val ExtraStatistics: Boolean  = false
  val IsLeaderRequired: Boolean = true
  val ResolveLinkTos: Boolean   = true

  val MessageTimeoutMs: Int                   = 30_000
  val MaxRetryCount: Int                      = 10
  val LiveBufferSize: Int                     = 500
  val HistoryBufferSize: Int                  = 500
  val ReadBatchSize: Int                      = 20
  val CheckpointAfterInMs: Int                = 2_000
  val CheckpointLowerBound: Int               = 10
  val CheckpointUpperBound: Int               = 1_000
  val MessageTimeoutInMs: Int                 = 30_000
  val MaxSubscriberCount: Int                 = 0
  val NamedConsumerStrategy: ConsumerStrategy = ConsumerStrategy.RoundRobin

}
