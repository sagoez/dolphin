// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting

import com.eventstore.dbclient.{DisableProjectionOptions, UserCredentials}

sealed abstract case class DisableProjectionSettings(
  private val options: DisableProjectionOptions
) extends BaseSettings[DisableProjectionSettings]
  with Product
  with Serializable { self =>

  private def copy(
    settings: DisableProjectionOptions
  ): DisableProjectionSettings = new DisableProjectionSettings(settings) {}

  override def withAuthentication(credentials: UserCredentials): DisableProjectionSettings = copy(
    options.authenticated(credentials)
  )

  override def withAuthentication(login: String, password: String): DisableProjectionSettings = copy(
    options.authenticated(login, password)
  )

  override def withDeadline(deadlineInMs: Long): DisableProjectionSettings = copy(options.deadline(deadlineInMs))

  override def withRequiredLeader(requiresLeader: Boolean): DisableProjectionSettings = copy(
    options.requiresLeader(requiresLeader)
  )

  private[dolphin] def toOptions: DisableProjectionOptions = options

}

object DisableProjectionSettings {

  /** Creates a new [[DisableProjectionSettings]] instance.
    * @return
    *   A [[DisableProjectionSettings]] instance.
    */
  val Default: DisableProjectionSettings = new DisableProjectionSettings(DisableProjectionOptions.get()) {}

}
