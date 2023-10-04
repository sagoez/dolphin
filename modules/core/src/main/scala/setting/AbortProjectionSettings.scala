// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting

import com.eventstore.dbclient.{AbortProjectionOptions, UserCredentials}

sealed abstract case class AbortProjectionSettings(private val options: AbortProjectionOptions)
  extends BaseSettings[AbortProjectionSettings]
  with Product
  with Serializable { self =>

  private def copy(settings: AbortProjectionOptions): AbortProjectionSettings = new AbortProjectionSettings(settings) {}

  override def withAuthentication(credentials: UserCredentials): AbortProjectionSettings = copy(
    options.authenticated(credentials)
  )

  override def withAuthentication(login: String, password: String): AbortProjectionSettings = copy(
    options.authenticated(login, password)
  )

  override def withDeadline(deadlineInMs: Long): AbortProjectionSettings = copy(options.deadline(deadlineInMs))

  override def withRequiredLeader(requiresLeader: Boolean): AbortProjectionSettings = copy(
    options.requiresLeader(requiresLeader)
  )

  private[dolphin] def toOptions: AbortProjectionOptions = options

}

object AbortProjectionSettings {

  /** Creates a new [[AbortProjectionSettings]] instance.
    * @return
    *   A [[AbortProjectionSettings]] instance.
    */
  val Default: AbortProjectionSettings = new AbortProjectionSettings(AbortProjectionOptions.get()) {}
}
