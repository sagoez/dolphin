// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting

import dolphin.concurrent.ConsumerStrategy

import com.eventstore.dbclient.{UpdatePersistentSubscriptionToStreamOptions, UserCredentials}

sealed abstract case class UpdatePersistentSubscriptionToStreamSettings private (
  private val options: UpdatePersistentSubscriptionToStreamOptions
) extends PersistentSubscriptionBaseSettings[UpdatePersistentSubscriptionToStreamSettings]
  with Product
  with Serializable {
  self =>

  private def copy(
    settings: UpdatePersistentSubscriptionToStreamOptions
  ): UpdatePersistentSubscriptionToStreamSettings = new UpdatePersistentSubscriptionToStreamSettings(settings) {}

  override def withAuthentication(login: String, password: String): UpdatePersistentSubscriptionToStreamSettings = copy(
    options.authenticated(new UserCredentials(login, password))
  )

  override def withAuthentication(credentials: UserCredentials): UpdatePersistentSubscriptionToStreamSettings = copy(
    options.authenticated(credentials)
  )

  override def withRequiredLeader(requiresLeader: Boolean): UpdatePersistentSubscriptionToStreamSettings = copy(
    options.requiresLeader(requiresLeader)
  )

  override def withDeadline(deadlineInMs: Long): UpdatePersistentSubscriptionToStreamSettings = copy(
    options.deadline(deadlineInMs)
  )

  override def withResolveLinkTos(resolveLinkTos: Boolean): UpdatePersistentSubscriptionToStreamSettings = copy(
    options.resolveLinkTos(resolveLinkTos)
  )

  override def withExtraStatistics(enabled: Boolean): UpdatePersistentSubscriptionToStreamSettings = copy(
    options.extraStatistics(enabled)
  )

  override def withCheckpointAfterInMs(checkpointAfterInMs: Int): UpdatePersistentSubscriptionToStreamSettings = copy(
    options.checkpointAfterInMs(checkpointAfterInMs)
  )

  override def withHistoryBufferSize(value: Int): UpdatePersistentSubscriptionToStreamSettings = copy(
    options.historyBufferSize(value)
  )

  override def withLiveBufferSize(value: Int): UpdatePersistentSubscriptionToStreamSettings = copy(
    options.liveBufferSize(value)
  )

  override def withCheckpointUpperBound(value: Int): UpdatePersistentSubscriptionToStreamSettings = copy(
    options.checkpointUpperBound(value)
  )

  override def withCheckpointLowerBound(value: Int): UpdatePersistentSubscriptionToStreamSettings = copy(
    options.checkpointLowerBound(value)
  )

  override def withMaxSubscriberCount(value: Int): UpdatePersistentSubscriptionToStreamSettings = copy(
    options.maxSubscriberCount(value)
  )

  override def withMaxRetryCount(value: Int): UpdatePersistentSubscriptionToStreamSettings = copy(
    options.maxRetryCount(value)
  )

  override def withMessageTimeoutInMs(messageTimeoutInMs: Int): UpdatePersistentSubscriptionToStreamSettings = copy(
    options.messageTimeoutInMs(messageTimeoutInMs)
  )

  override def withReadBatchSize(value: Int): UpdatePersistentSubscriptionToStreamSettings = copy(
    options.readBatchSize(value)
  )

  override def withNamedConsumerStrategy(value: ConsumerStrategy): UpdatePersistentSubscriptionToStreamSettings = copy(
    options.namedConsumerStrategy(value.toJava)
  )

  /** Starts the persistent subscription from the end of the stream. */
  def fromEnd: UpdatePersistentSubscriptionToStreamSettings = copy(
    options.fromEnd()
  )

  /** Starts the persistent subscription from the beginning of the stream. */
  def fromStart: UpdatePersistentSubscriptionToStreamSettings = copy(
    options.fromStart()
  )

  /** Starts the persistent subscription from a specific revision number. */
  def fromRevision(revision: Long): UpdatePersistentSubscriptionToStreamSettings = copy(
    options.startFrom(revision)
  )

  private[dolphin] def toOptions: UpdatePersistentSubscriptionToStreamOptions = options

}

object UpdatePersistentSubscriptionToStreamSettings {

  /** Returns an instance of [[UpdatePersistentSubscriptionToStreamSettings]] with default values.
    *
    * @return
    *   an instance of [[UpdatePersistentSubscriptionToStreamSettings]] with default values.
    */
  val Default: UpdatePersistentSubscriptionToStreamSettings =
    new UpdatePersistentSubscriptionToStreamSettings(
      UpdatePersistentSubscriptionToStreamOptions.get()
    ) {}

}
