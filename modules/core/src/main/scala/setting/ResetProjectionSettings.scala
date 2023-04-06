// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting

import com.eventstore.dbclient.{ResetProjectionOptions, UserCredentials}

sealed abstract case class ResetProjectionSettings(
  private val options: ResetProjectionOptions
) extends BaseSettings[ResetProjectionSettings]
  with Product
  with Serializable { self =>

  private def copy(settings: ResetProjectionOptions): ResetProjectionSettings = new ResetProjectionSettings(settings) {}

  override def withAuthentication(credentials: UserCredentials): ResetProjectionSettings = copy(
    options.authenticated(credentials)
  )

  override def withAuthentication(login: String, password: String): ResetProjectionSettings = copy(
    options.authenticated(login, password)
  )

  override def withDeadline(deadlineInMs: Long): ResetProjectionSettings = copy(options.deadline(deadlineInMs))

  override def withRequiredLeader(requiresLeader: Boolean): ResetProjectionSettings = copy(
    options.requiresLeader(requiresLeader)
  )

  private[dolphin] def toOptions: ResetProjectionOptions = options
}

object ResetProjectionSettings {

  /** Creates a new [[ResetProjectionSettings]] instance.
    * @return
    *   A [[ResetProjectionSettings]] instance.
    */
  val Default: ResetProjectionSettings = new ResetProjectionSettings(ResetProjectionOptions.get()) {}
}
