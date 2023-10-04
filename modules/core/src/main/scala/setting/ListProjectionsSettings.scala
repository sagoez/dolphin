// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting

import com.eventstore.dbclient.{ListProjectionsOptions, UserCredentials}

sealed abstract case class ListProjectionsSettings(
  private val options: ListProjectionsOptions
) extends BaseSettings[ListProjectionsSettings]
  with Product
  with Serializable { self =>

  private def copy(settings: ListProjectionsOptions): ListProjectionsSettings = new ListProjectionsSettings(settings) {}

  override def withAuthentication(credentials: UserCredentials): ListProjectionsSettings = copy(
    options.authenticated(credentials)
  )

  override def withAuthentication(login: String, password: String): ListProjectionsSettings = copy(
    options.authenticated(login, password)
  )

  override def withDeadline(deadlineInMs: Long): ListProjectionsSettings = copy(options.deadline(deadlineInMs))

  override def withRequiredLeader(requiresLeader: Boolean): ListProjectionsSettings = copy(
    options.requiresLeader(requiresLeader)
  )

  private[dolphin] def toOptions: ListProjectionsOptions = options
}

object ListProjectionsSettings {

  /** Creates a new [[ListProjectionsSettings]] instance.
    * @return
    *   A [[ListProjectionsSettings]] instance.
    */
  val Default: ListProjectionsSettings = new ListProjectionsSettings(ListProjectionsOptions.get()) {}
}
