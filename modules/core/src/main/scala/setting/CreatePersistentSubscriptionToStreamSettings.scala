// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting

import dolphin.concurrent.ConsumerStrategy

import com.eventstore.dbclient.{CreatePersistentSubscriptionToStreamOptions, UserCredentials}

sealed abstract case class CreatePersistentSubscriptionToStreamSettings(
  private val options: CreatePersistentSubscriptionToStreamOptions
) extends PersistentSubscriptionBaseSettings[CreatePersistentSubscriptionToStreamSettings]
  with Product
  with Serializable { self =>

  private def copy(
    settings: CreatePersistentSubscriptionToStreamOptions
  ): CreatePersistentSubscriptionToStreamSettings = new CreatePersistentSubscriptionToStreamSettings(settings) {}

  override def withNamedConsumerStrategy(value: ConsumerStrategy): CreatePersistentSubscriptionToStreamSettings = copy(
    options.namedConsumerStrategy(value.toJava)
  )

  override def withCheckpointAfterInMs(checkpointAfterInMs: Int): CreatePersistentSubscriptionToStreamSettings = copy(
    options.checkpointAfterInMs(checkpointAfterInMs)
  )

  override def withCheckpointLowerBound(value: Int): CreatePersistentSubscriptionToStreamSettings = copy(
    options.checkpointLowerBound(value)
  )

  override def withCheckpointUpperBound(value: Int): CreatePersistentSubscriptionToStreamSettings = copy(
    options.checkpointUpperBound(value)
  )

  override def withExtraStatistics(enabled: Boolean): CreatePersistentSubscriptionToStreamSettings = copy(
    options.extraStatistics(enabled)
  )

  override def withHistoryBufferSize(value: Int): CreatePersistentSubscriptionToStreamSettings = copy(
    options.historyBufferSize(value)
  )

  override def withLiveBufferSize(value: Int): CreatePersistentSubscriptionToStreamSettings = copy(
    options.liveBufferSize(value)
  )

  override def withMaxRetryCount(value: Int): CreatePersistentSubscriptionToStreamSettings = copy(
    options.maxRetryCount(value)
  )

  override def withMaxSubscriberCount(value: Int): CreatePersistentSubscriptionToStreamSettings = copy(
    options.maxSubscriberCount(value)
  )

  override def withMessageTimeoutInMs(messageTimeoutInMs: Int): CreatePersistentSubscriptionToStreamSettings = copy(
    options.messageTimeoutInMs(messageTimeoutInMs)
  )

  override def withReadBatchSize(value: Int): CreatePersistentSubscriptionToStreamSettings = copy(
    options.readBatchSize(value)
  )

  override def withResolveLinkTos(resolveLinkTos: Boolean): CreatePersistentSubscriptionToStreamSettings = copy(
    options.resolveLinkTos(resolveLinkTos)
  )

  override def withAuthentication(credentials: UserCredentials): CreatePersistentSubscriptionToStreamSettings = copy(
    options.authenticated(credentials)
  )

  override def withDeadline(deadlineInMs: Long): CreatePersistentSubscriptionToStreamSettings = copy(
    options.deadline(deadlineInMs)
  )

  override def withRequiredLeader(requiresLeader: Boolean): CreatePersistentSubscriptionToStreamSettings = copy(
    options.requiresLeader(requiresLeader)
  )

  override def withAuthentication(login: String, password: String): CreatePersistentSubscriptionToStreamSettings = copy(
    options.authenticated(login, password)
  )

  /** Starts from the end of the stream. */
  def fromEnd: CreatePersistentSubscriptionToStreamSettings = copy(
    options.fromEnd()
  )

  /** Starts the persistent subscription from the end. */
  def fromStart: CreatePersistentSubscriptionToStreamSettings = copy(
    options.fromStart()
  )

  private[dolphin] def toOptions = options
}

object CreatePersistentSubscriptionToStreamSettings {

  /** Create a new [[CreatePersistentSubscriptionToStreamSettings]] with default values.
    *
    * @return
    *   a new [[CreatePersistentSubscriptionToStreamSettings]] with default values.
    */
  val Default: CreatePersistentSubscriptionToStreamSettings =
    new CreatePersistentSubscriptionToStreamSettings(
      CreatePersistentSubscriptionToStreamOptions.get()
    ) {}

}
