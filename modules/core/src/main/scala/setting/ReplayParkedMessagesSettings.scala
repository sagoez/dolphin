// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting

import com.eventstore.dbclient.{ReplayParkedMessagesOptions, UserCredentials}

sealed abstract case class ReplayParkedMessagesSettings(
  private val options: ReplayParkedMessagesOptions
) extends BaseSettings[ReplayParkedMessagesSettings]
  with Product
  with Serializable { self =>

  private def copy(
    settings: ReplayParkedMessagesOptions
  ): ReplayParkedMessagesSettings = new ReplayParkedMessagesSettings(settings) {}

  override def withAuthentication(credentials: UserCredentials): ReplayParkedMessagesSettings = copy(
    options.authenticated(credentials)
  )

  override def withDeadline(deadlineInMs: Long): ReplayParkedMessagesSettings = copy(
    options.deadline(deadlineInMs)
  )

  override def withRequiredLeader(requiresLeader: Boolean): ReplayParkedMessagesSettings = copy(
    options.requiresLeader(requiresLeader)
  )

  override def withAuthentication(login: String, password: String): ReplayParkedMessagesSettings = copy(
    options.authenticated(login, password)
  )

  /** Replay the parked messages until the event revision within the parked messages stream is reached. */
  def stopAt(position: Long): ReplayParkedMessagesSettings = copy(
    options.stopAt(position)
  )

  private[dolphin] def toOptions = options

}

object ReplayParkedMessagesSettings {

  /** Creates a new [[ReplayParkedMessagesSettings]] instance.
    * @return
    *   a new [[ReplayParkedMessagesSettings]] instance
    */
  val Default: ReplayParkedMessagesSettings = new ReplayParkedMessagesSettings(ReplayParkedMessagesOptions.get()) {}
}
