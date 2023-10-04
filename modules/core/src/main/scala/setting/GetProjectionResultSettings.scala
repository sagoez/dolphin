// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting

import com.eventstore.dbclient.{GetProjectionResultOptions, UserCredentials}

sealed abstract case class GetProjectionResultSettings(
  private val options: GetProjectionResultOptions
) extends BaseSettings[GetProjectionResultSettings]
  with Product
  with Serializable {
  self =>
  private def copy(settings: GetProjectionResultOptions): GetProjectionResultSettings =
    new GetProjectionResultSettings(settings) {}

  override def withAuthentication(credentials: UserCredentials): GetProjectionResultSettings = copy(
    options.authenticated(credentials)
  )

  override def withAuthentication(login: String, password: String): GetProjectionResultSettings = copy(
    options.authenticated(login, password)
  )

  override def withDeadline(deadlineInMs: Long): GetProjectionResultSettings = copy(options.deadline(deadlineInMs))

  override def withRequiredLeader(requiresLeader: Boolean): GetProjectionResultSettings = copy(
    options.requiresLeader(requiresLeader)
  )

  /** Specifies which partition to retrieve the result from. */
  def withPartition(partition: String): GetProjectionResultSettings = copy(options.partition(partition))

  private[dolphin] def toOptions: GetProjectionResultOptions = options

}

object GetProjectionResultSettings {

  /** Creates a new [[GetProjectionResultSettings]] instance.
    * @return
    *   A [[GetProjectionResultSettings]] instance.
    */
  val Default: GetProjectionResultSettings = new GetProjectionResultSettings(GetProjectionResultOptions.get()) {}

}
