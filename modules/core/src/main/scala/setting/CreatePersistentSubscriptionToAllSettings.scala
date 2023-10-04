// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting

import dolphin.*
import dolphin.concurrent.ConsumerStrategy

import com.eventstore.dbclient.{CreatePersistentSubscriptionToAllOptions, UserCredentials}

// Handle authentication and authorization

sealed abstract case class CreatePersistentSubscriptionToAllSettings(
  private val options: CreatePersistentSubscriptionToAllOptions
) extends PersistentSubscriptionBaseSettings[CreatePersistentSubscriptionToAllSettings]
  with Product
  with Serializable { self =>

  private def copy(
    settings: CreatePersistentSubscriptionToAllOptions
  ): CreatePersistentSubscriptionToAllSettings = new CreatePersistentSubscriptionToAllSettings(settings) {}

  override def withAuthentication(login: String, password: String): CreatePersistentSubscriptionToAllSettings = copy(
    options.authenticated(login, password)
  )

  override def withAuthentication(credentials: UserCredentials): CreatePersistentSubscriptionToAllSettings = copy(
    options.authenticated(credentials)
  )

  override def withCheckpointAfterInMs(checkpointAfterInMs: Int): CreatePersistentSubscriptionToAllSettings = copy(
    options.checkpointAfterInMs(checkpointAfterInMs)
  )

  override def withCheckpointLowerBound(value: Int): CreatePersistentSubscriptionToAllSettings = copy(
    options.checkpointLowerBound(value)
  )

  override def withNamedConsumerStrategy(value: ConsumerStrategy): CreatePersistentSubscriptionToAllSettings = copy(
    options.namedConsumerStrategy(value.toJava)
  )

  override def withCheckpointUpperBound(value: Int): CreatePersistentSubscriptionToAllSettings = copy(
    options.checkpointUpperBound(value)
  )

  override def withExtraStatistics(enabled: Boolean): CreatePersistentSubscriptionToAllSettings = copy(
    options.extraStatistics(enabled)
  )

  override def withHistoryBufferSize(value: Int): CreatePersistentSubscriptionToAllSettings = copy(
    options.historyBufferSize(value)
  )

  override def withLiveBufferSize(value: Int): CreatePersistentSubscriptionToAllSettings = copy(
    options.liveBufferSize(value)
  )

  override def withMaxRetryCount(value: Int): CreatePersistentSubscriptionToAllSettings = copy(
    options.maxRetryCount(value)
  )

  override def withMaxSubscriberCount(value: Int): CreatePersistentSubscriptionToAllSettings = copy(
    options.maxSubscriberCount(value)
  )

  override def withMessageTimeoutInMs(messageTimeoutInMs: Int): CreatePersistentSubscriptionToAllSettings = copy(
    options.messageTimeoutInMs(messageTimeoutInMs)
  )

  override def withReadBatchSize(value: Int): CreatePersistentSubscriptionToAllSettings = copy(
    options.readBatchSize(value)
  )

  override def withResolveLinkTos(resolveLinkTos: Boolean): CreatePersistentSubscriptionToAllSettings = copy(
    options.resolveLinkTos(resolveLinkTos)
  )

  override def withDeadline(deadlineInMs: Long): CreatePersistentSubscriptionToAllSettings = copy(
    options.deadline(deadlineInMs)
  )

  override def withRequiredLeader(requiresLeader: Boolean): CreatePersistentSubscriptionToAllSettings = copy(
    options.requiresLeader(requiresLeader)
  )

  def startFrom(
    commitUnsigned: CommitUnsigned,
    prepareUnsigned: PrepareUnsigned
  ): CreatePersistentSubscriptionToAllSettings =
    new CreatePersistentSubscriptionToAllSettings(options.startFrom(commitUnsigned, prepareUnsigned)) {}

  def withFilter(
    filter: SubscriptionFilterSettings
  ): CreatePersistentSubscriptionToAllSettings =
    new CreatePersistentSubscriptionToAllSettings(
      options.filter(filter.build)
    ) {}

  def fromStart: CreatePersistentSubscriptionToAllSettings =
    new CreatePersistentSubscriptionToAllSettings(options.fromStart()) {}

  def fromEnd: CreatePersistentSubscriptionToAllSettings =
    new CreatePersistentSubscriptionToAllSettings(options.fromEnd()) {}

  private[dolphin] def toOptions = options

}

object CreatePersistentSubscriptionToAllSettings {

  /** Create a new [[CreatePersistentSubscriptionToAllSettings]] with default options.
    * @return
    *   A new [[CreatePersistentSubscriptionToAllSettings]] with default options.
    */
  val Default: CreatePersistentSubscriptionToAllSettings =
    new CreatePersistentSubscriptionToAllSettings(
      CreatePersistentSubscriptionToAllOptions.get()
    ) {}

}
