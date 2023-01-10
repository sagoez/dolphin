// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting

import com.eventstore.dbclient.{GetPersistentSubscriptionInfoOptions, UserCredentials}

sealed abstract case class GetPersistentSubscriptionInfoSettings(
  private val options: GetPersistentSubscriptionInfoOptions
) extends BaseSettings[GetPersistentSubscriptionInfoSettings]
  with Product
  with Serializable { self =>

  private def copy(
    settings: GetPersistentSubscriptionInfoOptions
  ): GetPersistentSubscriptionInfoSettings = new GetPersistentSubscriptionInfoSettings(settings) {}

  override def withRequiredLeader(requiresLeader: Boolean): GetPersistentSubscriptionInfoSettings = copy(
    settings = options.requiresLeader(requiresLeader)
  )

  override def withAuthentication(credentials: UserCredentials): GetPersistentSubscriptionInfoSettings = copy(
    settings = options.authenticated(credentials)
  )

  override def withDeadline(deadlineInMs: Long): GetPersistentSubscriptionInfoSettings = copy(
    settings = options.deadline(deadlineInMs)
  )

  override def withAuthentication(login: String, password: String): GetPersistentSubscriptionInfoSettings = copy(
    settings = options.authenticated(login, password)
  )

  private[dolphin] def toOptions = options

}

object GetPersistentSubscriptionInfoSettings {

  /** Default settings for [[GetPersistentSubscriptionInfoSettings]].
    *
    * @return
    *   [[GetPersistentSubscriptionInfoSettings]] with default settings.
    */

  val Default: GetPersistentSubscriptionInfoSettings =
    new GetPersistentSubscriptionInfoSettings(
      GetPersistentSubscriptionInfoOptions.get()
    ) {}
}
