// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting

import com.eventstore.dbclient.{DeletePersistentSubscriptionOptions, UserCredentials}

sealed abstract case class DeletePersistentSubscriptionSettings(
  private val options: DeletePersistentSubscriptionOptions
) extends BaseSettings[DeletePersistentSubscriptionSettings]
  with Product
  with Serializable { self =>

  private def copy(
    settings: DeletePersistentSubscriptionOptions
  ): DeletePersistentSubscriptionSettings = new DeletePersistentSubscriptionSettings(settings) {}

  override def withAuthentication(credentials: UserCredentials): DeletePersistentSubscriptionSettings = copy(
    options.authenticated(credentials)
  )

  override def withDeadline(deadlineInMs: Long): DeletePersistentSubscriptionSettings = copy(
    options.deadline(deadlineInMs)
  )

  override def withRequiredLeader(requiresLeader: Boolean): DeletePersistentSubscriptionSettings = copy(
    options.requiresLeader(requiresLeader)
  )

  override def withAuthentication(login: String, password: String): DeletePersistentSubscriptionSettings = copy(
    options.authenticated(login, password)
  )

  private[dolphin] def toOptions = options
}

object DeletePersistentSubscriptionSettings {

  /** Create a new instance of [[DeletePersistentSubscriptionSettings]].
    *
    * @return
    *   a new instance of [[DeletePersistentSubscriptionSettings]]
    */

  val Default: DeletePersistentSubscriptionSettings =
    new DeletePersistentSubscriptionSettings(DeletePersistentSubscriptionOptions.get()) {}

}
