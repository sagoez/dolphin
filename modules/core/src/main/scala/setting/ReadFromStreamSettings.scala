// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting

import com.eventstore.dbclient.{ReadStreamOptions, UserCredentials}

sealed abstract case class ReadFromStreamSettings(
  private val options: ReadStreamOptions
) extends BaseSettings[ReadFromStreamSettings]
  with Product
  with Serializable {
  self =>

  private def copy(
    settings: ReadStreamOptions
  ): ReadFromStreamSettings = new ReadFromStreamSettings(settings) {}

  override def withAuthentication(credentials: UserCredentials): ReadFromStreamSettings = copy(
    options.authenticated(credentials)
  )

  override def withDeadline(deadlineInMs: Long): ReadFromStreamSettings = copy(
    options.deadline(deadlineInMs)
  )

  override def withRequiredLeader(requiresLeader: Boolean): ReadFromStreamSettings = copy(
    options.requiresLeader(requiresLeader)
  )

  override def withAuthentication(login: String, password: String): ReadFromStreamSettings = copy(
    options.authenticated(login, password)
  )

  /** Reads stream in revision-descending order. */
  def backwards: ReadFromStreamSettings = copy(
    options.backwards()
  )

  /** Reads stream in revision-ascending order. */
  def forwards: ReadFromStreamSettings = copy(
    options.forwards()
  )

  /** Starts from the given event revision. */
  def fromRevision(revision: Long): ReadFromStreamSettings = copy(
    options.fromRevision(revision)
  )

  /** Starts from the beginning of the stream. */
  def fromStart: ReadFromStreamSettings = copy(
    options.fromStart()
  )

  /** Starts from the end of the stream. */
  def fromEnd: ReadFromStreamSettings = copy(
    options.fromEnd()
  )

  /** The maximum event count EventStoreDB will return. */
  def withMaxCount(maxCount: Long): ReadFromStreamSettings = copy(
    options.maxCount(maxCount)
  )

  /** Whether the subscription should resolve linkTo events to their linked events. */
  def resolveLinkTos(enabled: Boolean): ReadFromStreamSettings = copy(
    options.resolveLinkTos(enabled)
  )

  /** Resolve linkTo events to their linked events. */
  def resolveLinkTos: ReadFromStreamSettings = resolveLinkTos(true)

  /** Don't resolve linkTo events to their linked events. */
  def notResolveLinkTos: ReadFromStreamSettings = resolveLinkTos(false)

  private[dolphin] def toOptions = options
}

object ReadFromStreamSettings {

  /** Reads only the first event in the stream.
    *
    * @return
    *   a new [[ReadFromStreamSettings]] instance
    */
  val Default: ReadFromStreamSettings = new ReadFromStreamSettings(ReadStreamOptions.get()) {}

}
