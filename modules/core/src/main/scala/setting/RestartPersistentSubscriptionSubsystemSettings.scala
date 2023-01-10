// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting

import com.eventstore.dbclient.{RestartPersistentSubscriptionSubsystemOptions, UserCredentials}

sealed abstract case class RestartPersistentSubscriptionSubsystemSettings(
  private val options: RestartPersistentSubscriptionSubsystemOptions
) extends BaseSettings[RestartPersistentSubscriptionSubsystemSettings]
  with Product
  with Serializable {
  self =>

  private def copy(
    settings: RestartPersistentSubscriptionSubsystemOptions
  ): RestartPersistentSubscriptionSubsystemSettings = new RestartPersistentSubscriptionSubsystemSettings(settings) {}

  override def withAuthentication(
    credentials: UserCredentials
  ): RestartPersistentSubscriptionSubsystemSettings = copy(settings = options.authenticated(credentials))

  override def withDeadline(
    deadlineInMs: Long
  ): RestartPersistentSubscriptionSubsystemSettings = copy(settings = options.deadline(deadlineInMs))

  override def withRequiredLeader(
    requiresLeader: Boolean
  ): RestartPersistentSubscriptionSubsystemSettings = copy(settings = options.requiresLeader(requiresLeader))

  override def withAuthentication(login: String, password: String): RestartPersistentSubscriptionSubsystemSettings =
    copy(settings = options.authenticated(login, password))

  private[dolphin] def toOptions = options
}

object RestartPersistentSubscriptionSubsystemSettings {

  /** Creates a new instance of [[RestartPersistentSubscriptionSubsystemSettings]].
    * @return
    *   a new instance of [[RestartPersistentSubscriptionSubsystemSettings]].
    */
  val Default: RestartPersistentSubscriptionSubsystemSettings =
    new RestartPersistentSubscriptionSubsystemSettings(RestartPersistentSubscriptionSubsystemOptions.get()) {}
}
