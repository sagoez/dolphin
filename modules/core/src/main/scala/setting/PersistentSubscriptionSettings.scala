// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting

import com.eventstore.dbclient.{SubscribePersistentSubscriptionOptions, UserCredentials}

sealed abstract case class PersistentSubscriptionSettings(
  private val options: SubscribePersistentSubscriptionOptions
) extends BaseSettings[PersistentSubscriptionSettings]
  with Product
  with Serializable { self =>

  private def copy(options: SubscribePersistentSubscriptionOptions): PersistentSubscriptionSettings =
    new PersistentSubscriptionSettings(options) {}

  override def withAuthentication(credentials: UserCredentials): PersistentSubscriptionSettings = copy(
    options.authenticated(credentials)
  )

  override def withDeadline(deadlineInMs: Long): PersistentSubscriptionSettings = copy(
    options.deadline(deadlineInMs)
  )

  override def withRequiredLeader(requiresLeader: Boolean): PersistentSubscriptionSettings = copy(
    options.requiresLeader(requiresLeader)
  )

  override def withAuthentication(login: String, password: String): PersistentSubscriptionSettings = copy(
    options.authenticated(login, password)
  )

  /** Persistent subscription's buffer size. */
  def withBufferSize(bufferSize: Int): PersistentSubscriptionSettings = copy(
    options.bufferSize(bufferSize)
  )

  private[dolphin] def toOptions = options
}

object PersistentSubscriptionSettings {

  /** Creates a new instance of [[PersistentSubscriptionSettings]].
    *
    * @return
    *   a new [[PersistentSubscriptionSettings]] instance
    */
  val Default: PersistentSubscriptionSettings =
    new PersistentSubscriptionSettings(SubscribePersistentSubscriptionOptions.get()) {}
}
