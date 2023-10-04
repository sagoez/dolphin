// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting

import com.eventstore.dbclient.UserCredentials

trait BaseSettings[T] extends Product with Serializable {

  /** Sets user credentials for the request. */
  def withAuthentication(login: String, password: String): T

  /** Sets user credentials for the request. */
  def withAuthentication(credentials: UserCredentials): T

  /** Requires the request to be performed by the leader of the cluster. */
  def withLeaderRequired: T = withRequiredLeader(true)

  /** Do not require the request to be performed by the leader of the cluster. */
  def withNotLeaderRequired: T = withRequiredLeader(false)

  /** Requires the request to be performed by the leader of the cluster. */
  def withRequiredLeader(requiresLeader: Boolean): T

  /** A length of time (in milliseconds) to use for gRPC deadlines. */
  def withDeadline(deadlineInMs: Long): T
}
