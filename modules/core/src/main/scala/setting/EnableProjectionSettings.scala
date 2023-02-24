// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting

import com.eventstore.dbclient.{EnableProjectionOptions, UserCredentials}

sealed abstract case class EnableProjectionSettings(
  private val options: EnableProjectionOptions
) extends BaseSettings[EnableProjectionSettings]
  with Product
  with Serializable { self =>

  private def copy(
    settings: EnableProjectionOptions
  ): EnableProjectionSettings = new EnableProjectionSettings(settings) {}

  override def withAuthentication(credentials: UserCredentials): EnableProjectionSettings = copy(
    options.authenticated(credentials)
  )

  override def withAuthentication(login: String, password: String): EnableProjectionSettings = copy(
    options.authenticated(login, password)
  )

  override def withDeadline(deadlineInMs: Long): EnableProjectionSettings = copy(options.deadline(deadlineInMs))

  override def withRequiredLeader(requiresLeader: Boolean): EnableProjectionSettings = copy(
    options.requiresLeader(requiresLeader)
  )

  private[dolphin] def toOptions: EnableProjectionOptions = options

}

object EnableProjectionSettings {

  /** Creates a new [[EnableProjectionSettings]] instance.
    * @return
    *   A [[EnableProjectionSettings]] instance.
    */
  val Default: EnableProjectionSettings = new EnableProjectionSettings(EnableProjectionOptions.get()) {}

}
