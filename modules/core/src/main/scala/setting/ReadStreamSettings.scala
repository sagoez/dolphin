// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting

import com.eventstore.dbclient.{ReadStreamOptions, UserCredentials}

sealed abstract case class ReadStreamSettings(
  private val options: ReadStreamOptions
) extends BaseSettings[ReadStreamSettings]
  with Product
  with Serializable {
  self =>

  private def copy(
    settings: ReadStreamOptions
  ): ReadStreamSettings = new ReadStreamSettings(settings) {}

  override def withAuthentication(credentials: UserCredentials): ReadStreamSettings = copy(
    options.authenticated(credentials)
  )

  override def withDeadline(deadlineInMs: Long): ReadStreamSettings = copy(
    options.deadline(deadlineInMs)
  )

  override def withRequiredLeader(requiresLeader: Boolean): ReadStreamSettings = copy(
    options.requiresLeader(requiresLeader)
  )

  override def withAuthentication(login: String, password: String): ReadStreamSettings = copy(
    options.authenticated(login, password)
  )

  /** Reads stream in revision-descending order. */
  def backwards: ReadStreamSettings = copy(
    options.backwards()
  )

  /** Reads stream in revision-ascending order. */
  def forwards: ReadStreamSettings = copy(
    options.forwards()
  )

  /** Starts from the given event revision. */
  def fromRevision(revision: Long): ReadStreamSettings = copy(
    options.fromRevision(revision)
  )

  /** Starts from the beginning of the stream. */
  def fromStart: ReadStreamSettings = copy(
    options.fromStart()
  )

  /** Starts from the end of the stream. */
  def fromEnd: ReadStreamSettings = copy(
    options.fromEnd()
  )

  /** The maximum event count EventStoreDB will return. */
  def withMaxCount(maxCount: Long): ReadStreamSettings = copy(
    options.maxCount(maxCount)
  )

  /** Whether the subscription should resolve linkTo events to their linked events. */
  def resolveLinkTos(enabled: Boolean): ReadStreamSettings = copy(
    options.resolveLinkTos(enabled)
  )

  /** Resolve linkTo events to their linked events. */
  def resolveLinkTos: ReadStreamSettings = resolveLinkTos(true)

  /** Don't resolve linkTo events to their linked events. */
  def notResolveLinkTos: ReadStreamSettings = resolveLinkTos(false)

  private[dolphin] def toOptions = options
}

object ReadStreamSettings {

  /** Reads only the first event in the stream.
    *
    * @return
    *   a new [[ReadStreamSettings]] instance
    */
  val Default: ReadStreamSettings = new ReadStreamSettings(ReadStreamOptions.get()) {}

}
