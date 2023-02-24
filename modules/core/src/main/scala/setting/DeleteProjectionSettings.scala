// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting

import com.eventstore.dbclient.{DeleteProjectionOptions, UserCredentials}

sealed abstract case class DeleteProjectionSettings(
  private val options: DeleteProjectionOptions
) extends BaseSettings[DeleteProjectionSettings]
  with Product
  with Serializable { self =>

  private def copy(settings: DeleteProjectionOptions): DeleteProjectionSettings =
    new DeleteProjectionSettings(settings) {}

  override def withAuthentication(credentials: UserCredentials): DeleteProjectionSettings = copy(
    options.authenticated(credentials)
  )

  override def withAuthentication(login: String, password: String): DeleteProjectionSettings = copy(
    options.authenticated(login, password)
  )

  override def withDeadline(deadlineInMs: Long): DeleteProjectionSettings = copy(options.deadline(deadlineInMs))

  override def withRequiredLeader(requiresLeader: Boolean): DeleteProjectionSettings = copy(
    options.requiresLeader(requiresLeader)
  )

  /** Deletes the projection checkpoint stream. */
  def withDeleteCheckpointStream: DeleteProjectionSettings = copy(
    options.deleteCheckpointStream()
  )

  /** If true, deletes the projection checkpoint stream. */
  def withDeleteCheckpointStream(enabled: Boolean): DeleteProjectionSettings = copy(
    options.deleteCheckpointStream(enabled)
  )

  /** Deletes emitted streams if the projections had track emitted streams enabled. */
  def withDeleteEmittedStreams(enabled: Boolean): DeleteProjectionSettings = copy(options.deleteEmittedStreams(enabled))

  /** If true, deletes emitted streams if the projections had track emitted streams enabled. */
  def withDeleteEmittedStreams: DeleteProjectionSettings = copy(options.deleteEmittedStreams())

  /** Deletes the projection state stream. */
  def withDeleteStateStream: DeleteProjectionSettings = copy(options.deleteStateStream())

  /** If true, deletes the projection state stream. */
  def withDeleteStateStream(enabled: Boolean): DeleteProjectionSettings = copy(options.deleteStateStream(enabled))

  private[dolphin] def toOptions: DeleteProjectionOptions = options

}

object DeleteProjectionSettings {

  /** Creates a new [[DeleteProjectionSettings]] instance.
    * @return
    *   A [[DeleteProjectionSettings]] instance.
    */
  val Default: DeleteProjectionSettings = new DeleteProjectionSettings(DeleteProjectionOptions.get()) {}

}
