// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting

import dolphin.concurrent.ExpectedRevision
import dolphin.concurrent.ExpectedRevision.ExpectedRevisionOps

import com.eventstore.dbclient.{AppendToStreamOptions, UserCredentials}

sealed abstract case class AppendToStreamSettings(
  private val options: AppendToStreamOptions
) extends BaseSettings[AppendToStreamSettings]
  with Product
  with Serializable {
  self =>

  private def copy(
    settings: AppendToStreamOptions
  ): AppendToStreamSettings = new AppendToStreamSettings(settings) {}

  override def withAuthentication(credentials: UserCredentials): AppendToStreamSettings = copy(
    options.authenticated(credentials)
  )

  override def withDeadline(deadlineInMs: Long): AppendToStreamSettings = copy(
    options.deadline(deadlineInMs)
  )

  override def withRequiredLeader(requiresLeader: Boolean): AppendToStreamSettings = copy(
    options.requiresLeader(requiresLeader)
  )

  override def withAuthentication(login: String, password: String): AppendToStreamSettings = copy(
    options.authenticated(login, password)
  )

  /** Asks the server to check that the stream receiving is at the given expected version. */
  def withExpectedRevision(expectedRevision: ExpectedRevision): AppendToStreamSettings = copy(
    options.expectedRevision(expectedRevision.toJava)
  )

  /** Asks the server to check that the stream receiving is at the given expected version. */
  def withExpectedRevision(expectedRevision: Long): AppendToStreamSettings = copy(
    options.expectedRevision(expectedRevision)
  )

  private[dolphin] def toOptions = options
}

object AppendToStreamSettings {

  /** Create a new instance of [[AppendToStreamSettings]].
    * @return
    *   a new instance of [[AppendToStreamSettings]]
    */
  val Default: AppendToStreamSettings = new AppendToStreamSettings(AppendToStreamOptions.get()) {}

}
