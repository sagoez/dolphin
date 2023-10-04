// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting

import com.eventstore.dbclient.{GetProjectionStatusOptions, UserCredentials}

sealed abstract case class GetProjectionStatusSettings(
  private val options: GetProjectionStatusOptions
) extends BaseSettings[GetProjectionStatusSettings]
  with Product
  with Serializable { self =>

  private def copy(settings: GetProjectionStatusOptions): GetProjectionStatusSettings =
    new GetProjectionStatusSettings(settings) {}

  override def withAuthentication(credentials: UserCredentials): GetProjectionStatusSettings = copy(
    options.authenticated(credentials)
  )

  override def withAuthentication(login: String, password: String): GetProjectionStatusSettings = copy(
    options.authenticated(login, password)
  )

  override def withDeadline(deadlineInMs: Long): GetProjectionStatusSettings = copy(options.deadline(deadlineInMs))

  override def withRequiredLeader(requiresLeader: Boolean): GetProjectionStatusSettings = copy(
    options.requiresLeader(requiresLeader)
  )

  private[dolphin] def toOptions: GetProjectionStatusOptions = options
}

object GetProjectionStatusSettings {

  /** Creates a new [[GetProjectionStatusSettings]] instance.
    * @return
    *   A [[GetProjectionStatusSettings]] instance.
    */
  val Default: GetProjectionStatusSettings = new GetProjectionStatusSettings(GetProjectionStatusOptions.get()) {}
}
