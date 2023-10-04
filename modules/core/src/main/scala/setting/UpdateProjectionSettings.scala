// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting

import com.eventstore.dbclient.{UpdateProjectionOptions, UserCredentials}

sealed abstract case class UpdateProjectionSettings(
  private val options: UpdateProjectionOptions
) extends BaseSettings[UpdateProjectionSettings]
  with Product
  with Serializable { self =>

  private def copy(settings: UpdateProjectionOptions): UpdateProjectionSettings =
    new UpdateProjectionSettings(settings) {}

  override def withAuthentication(credentials: UserCredentials): UpdateProjectionSettings = copy(
    options.authenticated(credentials)
  )

  override def withAuthentication(login: String, password: String): UpdateProjectionSettings = copy(
    options.authenticated(login, password)
  )

  override def withDeadline(deadlineInMs: Long): UpdateProjectionSettings = copy(options.deadline(deadlineInMs))

  override def withRequiredLeader(requiresLeader: Boolean): UpdateProjectionSettings = copy(
    options.requiresLeader(requiresLeader)
  )

  private[dolphin] def toOptions: UpdateProjectionOptions = options
}

object UpdateProjectionSettings {

  /** Creates a new [[UpdateProjectionSettings]] instance.
    * @return
    *   A [[UpdateProjectionSettings]] instance.
    */
  val Default: UpdateProjectionSettings = new UpdateProjectionSettings(UpdateProjectionOptions.get()) {}
}
