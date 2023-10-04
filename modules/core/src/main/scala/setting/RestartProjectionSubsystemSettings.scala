// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting

import com.eventstore.dbclient.{RestartProjectionSubsystemOptions, UserCredentials}

sealed abstract case class RestartProjectionSubsystemSettings(
  private val options: RestartProjectionSubsystemOptions
) extends BaseSettings[RestartProjectionSubsystemSettings]
  with Product
  with Serializable { self =>

  private def copy(settings: RestartProjectionSubsystemOptions): RestartProjectionSubsystemSettings =
    new RestartProjectionSubsystemSettings(settings) {}

  override def withAuthentication(credentials: UserCredentials): RestartProjectionSubsystemSettings = copy(
    options.authenticated(credentials)
  )

  override def withAuthentication(login: String, password: String): RestartProjectionSubsystemSettings = copy(
    options.authenticated(login, password)
  )

  override def withDeadline(deadlineInMs: Long): RestartProjectionSubsystemSettings = copy(
    options.deadline(deadlineInMs)
  )

  override def withRequiredLeader(requiresLeader: Boolean): RestartProjectionSubsystemSettings = copy(
    options.requiresLeader(requiresLeader)
  )

  private[dolphin] def toOptions: RestartProjectionSubsystemOptions = options
}

object RestartProjectionSubsystemSettings {

  /** Creates a new [[RestartProjectionSubsystemSettings]] instance.
    * @return
    *   A [[RestartProjectionSubsystemSettings]] instance.
    */
  val Default: RestartProjectionSubsystemSettings =
    new RestartProjectionSubsystemSettings(RestartProjectionSubsystemOptions.get()) {}
}
