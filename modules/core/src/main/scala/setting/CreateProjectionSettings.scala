// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting

import com.eventstore.dbclient.{CreateProjectionOptions, UserCredentials}

sealed abstract case class CreateProjectionSettings(
  private val options: CreateProjectionOptions
) extends BaseSettings[CreateProjectionSettings]
  with Product
  with Serializable {
  self =>
  private def copy(settings: CreateProjectionOptions): CreateProjectionSettings =
    new CreateProjectionSettings(settings) {}

  override def withAuthentication(credentials: UserCredentials): CreateProjectionSettings = copy(
    options.authenticated(credentials)
  )

  override def withDeadline(deadlineInMs: Long): CreateProjectionSettings = copy(options.deadline(deadlineInMs))

  override def withRequiredLeader(requiresLeader: Boolean): CreateProjectionSettings = copy(
    options.requiresLeader(requiresLeader)
  )

  override def withAuthentication(login: String, password: String): CreateProjectionSettings = copy(
    options.authenticated(login, password)
  )

  /** If true, allows the projection to emit events. */
  def withEmitEnabled(enabled: Boolean): CreateProjectionSettings = copy(options.emitEnabled(enabled))

  /** If true, the projection tracks all streams it creates. */
  def withTrackEmittedStreams(enabled: Boolean): CreateProjectionSettings = copy(options.trackEmittedStreams(enabled))

  private[dolphin] def toOptions: CreateProjectionOptions = options
}

object CreateProjectionSettings {

  /** Creates a new [[CreateProjectionSettings]] instance.
    * @return
    *   A [[CreateProjectionSettings]] instance.
    */
  val Default: CreateProjectionSettings = new CreateProjectionSettings(CreateProjectionOptions.get()) {}

}
