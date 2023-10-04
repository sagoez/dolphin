// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting

import dolphin.concurrent.ExpectedRevision

import com.eventstore.dbclient.{DeleteStreamOptions, UserCredentials}

sealed abstract case class DeleteStreamSettings(
  private val options: DeleteStreamOptions
) extends BaseSettings[DeleteStreamSettings]
  with Product
  with Serializable {
  self =>

  private def copy(
    settings: DeleteStreamOptions
  ): DeleteStreamSettings = new DeleteStreamSettings(settings) {}

  override def withAuthentication(credentials: UserCredentials): DeleteStreamSettings = copy(
    settings = options.authenticated(credentials)
  )

  override def withDeadline(deadlineInMs: Long): DeleteStreamSettings = copy(
    settings = options.deadline(deadlineInMs)
  )

  override def withRequiredLeader(requiresLeader: Boolean): DeleteStreamSettings = copy(
    settings = options.requiresLeader(requiresLeader)
  )

  override def withAuthentication(login: String, password: String): DeleteStreamSettings = copy(
    settings = options.authenticated(login, password)
  )

  /** Asks the server to check that the stream receiving is at the given expected version. */
  def withExpectedRevision(expectedRevision: ExpectedRevision): DeleteStreamSettings = copy(
    settings = options.expectedRevision(expectedRevision.toJava)
  )

  /** Asks the server to check that the stream receiving is at the given expected version. */
  def withExpectedRevision(expectedRevision: Long): DeleteStreamSettings = copy(
    settings = options.expectedRevision(expectedRevision)
  )

  private[dolphin] def toOptions = options

}

// Adding this for sake of completeness, but it's not needed
object DeleteStreamSettings {

  /** Create a new instance of [[DeleteStreamSettings]].
    *
    * @return
    *   a new instance of [[DeleteStreamSettings]]
    */

  val Default: DeleteStreamSettings = new DeleteStreamSettings(DeleteStreamOptions.get()) {}

}
