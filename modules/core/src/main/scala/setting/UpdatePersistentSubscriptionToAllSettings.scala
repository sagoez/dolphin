// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting

import dolphin.concurrent.{ConsumerStrategy, Position}
import dolphin.{CommitUnsigned, PrepareUnsigned}

import com.eventstore.dbclient.{UpdatePersistentSubscriptionToAllOptions, UserCredentials}

sealed abstract case class UpdatePersistentSubscriptionToAllSettings(
  private val options: UpdatePersistentSubscriptionToAllOptions
) extends PersistentSubscriptionBaseSettings[UpdatePersistentSubscriptionToAllSettings]
  with Product
  with Serializable {
  self =>

  private def copy(options: UpdatePersistentSubscriptionToAllOptions): UpdatePersistentSubscriptionToAllSettings =
    new UpdatePersistentSubscriptionToAllSettings(options) {}

  override def withNamedConsumerStrategy(value: ConsumerStrategy): UpdatePersistentSubscriptionToAllSettings = copy(
    options.namedConsumerStrategy(value.toJava)
  )

  override def withCheckpointAfterInMs(checkpointAfterInMs: Int): UpdatePersistentSubscriptionToAllSettings = copy(
    options.checkpointAfterInMs(checkpointAfterInMs)
  )

  override def withCheckpointLowerBound(value: Int): UpdatePersistentSubscriptionToAllSettings = copy(
    options.checkpointLowerBound(value)
  )

  override def withCheckpointUpperBound(value: Int): UpdatePersistentSubscriptionToAllSettings = copy(
    options.checkpointUpperBound(value)
  )

  override def withExtraStatistics(enabled: Boolean): UpdatePersistentSubscriptionToAllSettings = copy(
    options.extraStatistics(enabled)
  )

  override def withHistoryBufferSize(value: Int): UpdatePersistentSubscriptionToAllSettings = copy(
    options.historyBufferSize(value)
  )

  override def withLiveBufferSize(value: Int): UpdatePersistentSubscriptionToAllSettings = copy(
    options.liveBufferSize(value)
  )

  override def withMaxRetryCount(value: Int): UpdatePersistentSubscriptionToAllSettings = copy(
    options.maxRetryCount(value)
  )

  override def withMaxSubscriberCount(value: Int): UpdatePersistentSubscriptionToAllSettings = copy(
    options.maxSubscriberCount(value)
  )

  override def withMessageTimeoutInMs(messageTimeoutInMs: Int): UpdatePersistentSubscriptionToAllSettings = copy(
    options.messageTimeoutInMs(messageTimeoutInMs)
  )

  override def withReadBatchSize(value: Int): UpdatePersistentSubscriptionToAllSettings = copy(
    options.readBatchSize(value)
  )

  override def withResolveLinkTos(resolveLinkTos: Boolean): UpdatePersistentSubscriptionToAllSettings = copy(
    options.resolveLinkTos(resolveLinkTos)
  )

  override def withAuthentication(credentials: UserCredentials): UpdatePersistentSubscriptionToAllSettings = copy(
    options.authenticated(credentials)
  )

  override def withDeadline(deadlineInMs: Long): UpdatePersistentSubscriptionToAllSettings = copy(
    options.deadline(deadlineInMs)
  )

  override def withRequiredLeader(requiresLeader: Boolean): UpdatePersistentSubscriptionToAllSettings = copy(
    options.requiresLeader(requiresLeader)
  )

  override def withAuthentication(login: String, password: String): UpdatePersistentSubscriptionToAllSettings = copy(
    options.authenticated(login, password)
  )

  /** Starts the persistent subscription from the end of the <b>\$all</b> stream. */
  def fromEnd: UpdatePersistentSubscriptionToAllSettings = copy(
    options.fromEnd()
  )

  /** Starts the persistent subscription from the beginning of the <b>\$all</b> stream. */
  def fromStart: UpdatePersistentSubscriptionToAllSettings = copy(
    options.fromStart()
  )

  /** Starts the persistent subscription from a specific transaction log position. */
  def startFrom(
    prepareUnsigned: PrepareUnsigned,
    commitUnsigned: CommitUnsigned
  ): UpdatePersistentSubscriptionToAllSettings = copy(
    options.startFrom(prepareUnsigned, commitUnsigned)
  )

  /** Starts the persistent subscription from a specific transaction log position. */
  def startFrom(
    position: Position
  ): UpdatePersistentSubscriptionToAllSettings = copy(options.startFrom(position.toJava))

  private[dolphin] def toOptions = options

}

object UpdatePersistentSubscriptionToAllSettings {

  /** Creates a new instance of [[UpdatePersistentSubscriptionToAllSettings]].
    *
    * @return
    *   a new instance of [[UpdatePersistentSubscriptionToAllSettings]]
    */
  val Default: UpdatePersistentSubscriptionToAllSettings =
    new UpdatePersistentSubscriptionToAllSettings(UpdatePersistentSubscriptionToAllOptions.get()) {}

}
