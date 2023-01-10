// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting

import dolphin.concurrent.StreamPosition

import com.eventstore.dbclient.{SubscribeToStreamOptions, UserCredentials}

// Handle authentication and authorization

sealed abstract case class SubscriptionToStreamSettings(
  private val options: SubscribeToStreamOptions
) extends BaseSettings[SubscriptionToStreamSettings]
  with Product
  with Serializable { self =>

  private def copy(options: SubscribeToStreamOptions): SubscriptionToStreamSettings =
    new SubscriptionToStreamSettings(options) {}

  override def withAuthentication(credentials: UserCredentials): SubscriptionToStreamSettings = copy(
    options.authenticated(credentials)
  )

  override def withDeadline(deadlineInMs: Long): SubscriptionToStreamSettings = copy(options.deadline(deadlineInMs))

  override def withRequiredLeader(requiresLeader: Boolean): SubscriptionToStreamSettings = copy(
    options.requiresLeader(requiresLeader)
  )

  override def withAuthentication(login: String, password: String): SubscriptionToStreamSettings = copy(
    options.authenticated(login, password)
  )

  /** Starts from the given event revision. */
  def fromRevision(revision: Long): SubscriptionToStreamSettings = copy(options.fromRevision(revision))

  /** Starts from the given event position. */
  def fromRevision(position: StreamPosition[Long]): SubscriptionToStreamSettings =
    position match {
      case StreamPosition.Exact(value) => copy(options.fromRevision(value))
      case StreamPosition.Start        => copy(options.fromStart())
      case StreamPosition.End          => copy(options.fromEnd())
    }

  /** Starts from the end of the stream. */
  def fromEnd: SubscriptionToStreamSettings = copy(options.fromEnd())

  /** Starts from the beginning of the stream. */
  def fromStart: SubscriptionToStreamSettings = copy(options.fromStart())

  /** Whether the subscription should resolve linkTo events to their linked events. */
  def withResolveLinkTos(resolveLinkTos: Boolean): SubscriptionToStreamSettings = copy(
    options.resolveLinkTos(resolveLinkTos)
  )

  /** Resolve linkTo events to their linked events. */
  def withResolveLinkTos: SubscriptionToStreamSettings = withResolveLinkTos(true)

  /** Don't resolve linkTo events to their linked events. */
  def notResolveLinkTos: SubscriptionToStreamSettings = withResolveLinkTos(false)

  private[dolphin] def toOptions = options
}

object SubscriptionToStreamSettings {

  /** Creates a new instance of [[SubscriptionToStreamSettings]].
    *
    * @return
    *   a new [[SubscriptionToStreamSettings]] instance
    */
  val Default: SubscriptionToStreamSettings = new SubscriptionToStreamSettings(SubscribeToStreamOptions.get()) {}
}
