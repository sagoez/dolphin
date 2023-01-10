// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting

import scala.concurrent.duration.Duration

import dolphin.concurrent.ConsumerStrategy

trait PersistentSubscriptionBaseSettings[T] extends BaseSettings[T] with Product with Serializable {

  /** Resolve linktos. */
  def resolveLinkTos: T = withResolveLinkTos(true)

  /** Not resolve linktos. */
  def notResolveLinkTos: T = withResolveLinkTos(false)

  /** Whether the subscription should resolve linkTo events to their linked events. */
  def withResolveLinkTos(resolveLinkTos: Boolean): T

  /** Enables the tracking of latency statistics on this subscription.
    */
  def enableExtraStatistics: T = withExtraStatistics(true)

  /** Disable the tracking of latency statistics on this subscription.
    */
  def disableExtraStatistics: T = withExtraStatistics(false)

  /** Whether to track latency statistics on this subscription.
    */
  def withExtraStatistics(enabled: Boolean): T

  /** The amount of time to try to checkpoint after.
    */
  def withCheckpointAfter(value: Duration): T = withCheckpointAfterInMs(value.toMillis.toInt)

  /** The amount of time to try to checkpoint in millis.
    */
  def withCheckpointAfterInMs(checkpointAfterInMs: Int): T

  /** The number of events to cache when catching up. */
  def withHistoryBufferSize(value: Int): T

  /** The size of the buffer (in-memory) listening to live messages as they happen before paging occurs.
    */
  def withLiveBufferSize(value: Int): T

  /** The maximum number of messages to process before a checkpoint may be written.
    */
  def withCheckpointUpperBound(value: Int): T

  /** The minimum number of messages to process before a checkpoint may be written.
    */
  def withCheckpointLowerBound(value: Int): T

  /** The maximum number of subscribers allowed.
    */
  def withMaxSubscriberCount(value: Int): T

  /** The maximum number of retries (due to timeout) before a message is considered to be parked. */
  def withMaxRetryCount(value: Int): T

  /** The amount of time in milliseconds after which to consider a message as timed out and retried. */
  def withMessageTimeout(value: Duration): T = withMessageTimeoutInMs(value.toMillis.toInt)

  /** The amount of time in milliseconds after which to consider a message as timed out and retried in millis. */
  def withMessageTimeoutInMs(messageTimeoutInMs: Int): T

  /** The number of events read at a time when catching up. */
  def withReadBatchSize(value: Int): T

  /** The strategy to use for distributing events to client consumers. */
  def withNamedConsumerStrategy(value: ConsumerStrategy): T
}
