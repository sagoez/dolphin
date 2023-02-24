// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting

import com.eventstore.dbclient.{GetProjectionStateOptions, UserCredentials}

sealed abstract case class GetProjectionStateSettings(
  private val options: GetProjectionStateOptions
) extends BaseSettings[GetProjectionStateSettings]
  with Product
  with Serializable {
  self =>
  private def copy(settings: GetProjectionStateOptions): GetProjectionStateSettings =
    new GetProjectionStateSettings(settings) {}

  override def withAuthentication(credentials: UserCredentials): GetProjectionStateSettings = copy(
    options.authenticated(credentials)
  )

  override def withAuthentication(login: String, password: String): GetProjectionStateSettings = copy(
    options.authenticated(login, password)
  )

  override def withDeadline(deadlineInMs: Long): GetProjectionStateSettings = copy(options.deadline(deadlineInMs))

  override def withRequiredLeader(requiresLeader: Boolean): GetProjectionStateSettings = copy(
    options.requiresLeader(requiresLeader)
  )

  /** Specifies which partition to retrieve the result from. */
  def withPartition(partition: String): GetProjectionStateSettings = copy(options.partition(partition))

  private[dolphin] def toOptions: GetProjectionStateOptions = options
}

object GetProjectionStateSettings {

  /** Creates a new [[GetProjectionStateSettings]] instance.
    * @return
    *   A [[GetProjectionStateSettings]] instance.
    */
  val Default: GetProjectionStateSettings = new GetProjectionStateSettings(GetProjectionStateOptions.get()) {}

}
