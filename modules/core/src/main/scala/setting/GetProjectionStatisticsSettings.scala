// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting

import com.eventstore.dbclient.{GetProjectionStatisticsOptions, UserCredentials}

sealed abstract case class GetProjectionStatisticsSettings(
  private val options: GetProjectionStatisticsOptions
) extends BaseSettings[GetProjectionStatisticsSettings]
  with Product
  with Serializable { self =>

  private def copy(settings: GetProjectionStatisticsOptions): GetProjectionStatisticsSettings =
    new GetProjectionStatisticsSettings(settings) {}

  override def withAuthentication(credentials: UserCredentials): GetProjectionStatisticsSettings = copy(
    options.authenticated(credentials)
  )

  override def withAuthentication(login: String, password: String): GetProjectionStatisticsSettings = copy(
    options.authenticated(login, password)
  )

  override def withDeadline(deadlineInMs: Long): GetProjectionStatisticsSettings = copy(options.deadline(deadlineInMs))

  override def withRequiredLeader(requiresLeader: Boolean): GetProjectionStatisticsSettings = copy(
    options.requiresLeader(requiresLeader)
  )

  private[dolphin] def toOptions: GetProjectionStatisticsOptions = options
}

object GetProjectionStatisticsSettings {

  /** Creates a new [[GetProjectionStatisticsSettings]] instance.
    * @return
    *   A [[GetProjectionStatisticsSettings]] instance.
    */
  val Default: GetProjectionStatisticsSettings =
    new GetProjectionStatisticsSettings(GetProjectionStatisticsOptions.get()) {}
}
