// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting

import com.eventstore.dbclient.{ListPersistentSubscriptionsOptions, UserCredentials}

sealed abstract case class ListPersistentSubscriptionsSettings(
  private val options: ListPersistentSubscriptionsOptions
) extends BaseSettings[ListPersistentSubscriptionsSettings]
  with Product
  with Serializable { self =>

  private def copy(
    settings: ListPersistentSubscriptionsOptions
  ): ListPersistentSubscriptionsSettings = new ListPersistentSubscriptionsSettings(settings) {}

  override def withAuthentication(credentials: UserCredentials): ListPersistentSubscriptionsSettings = copy(
    settings = options.authenticated(credentials)
  )

  override def withDeadline(deadlineInMs: Long): ListPersistentSubscriptionsSettings = copy(
    settings = options.deadline(deadlineInMs)
  )

  override def withRequiredLeader(requiresLeader: Boolean): ListPersistentSubscriptionsSettings = copy(
    settings = options.requiresLeader(requiresLeader)
  )

  override def withAuthentication(login: String, password: String): ListPersistentSubscriptionsSettings = copy(
    settings = options.authenticated(login, password)
  )

  private[dolphin] def toOptions: ListPersistentSubscriptionsOptions = options
}

object ListPersistentSubscriptionsSettings {

  /** Default settings for [[ListPersistentSubscriptionsSettings]].
    *
    * @return
    *   [[ListPersistentSubscriptionsSettings]] with default settings.
    */
  val Default: ListPersistentSubscriptionsSettings =
    new ListPersistentSubscriptionsSettings(
      ListPersistentSubscriptionsOptions.get()
    ) {}
}
