// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.option

import scala.util.Try

import com.eventstore.dbclient.SubscribeToStreamOptions

sealed abstract case class SubscriptionOptions private () extends Product with Serializable {
  self =>

  protected[dolphin] def get: Try[SubscribeToStreamOptions] = Try(SubscribeToStreamOptions.get())

  /** If true, requires the request to be performed by the leader of the cluster.
    *
    * @param isRequired
    */
  def withLeaderRequired(isRequired: Boolean): SubscriptionOptions =
    new SubscriptionOptions {
      override def get: Try[SubscribeToStreamOptions] = self.get.map(_.requiresLeader(isRequired))
    }

  /** Whether the subscription should resolve linkTo events to their linked events. Default: false.
    * @param resolveLinkTos
    *   whether the subscription should resolve linkTo events to their linked events.
    */
  def withResolveLinkTos(resolveLinkTos: Boolean): SubscriptionOptions =
    new SubscriptionOptions {
      override def get: Try[SubscribeToStreamOptions] = self.get.map(_.resolveLinkTos(resolveLinkTos))
    }

  /** A length of time (in milliseconds) to use for gRPC deadlines.
    *
    * @param durationInMs
    */
  def withDeadline(durationInMs: Long): SubscriptionOptions =
    new SubscriptionOptions {
      override def get: Try[SubscribeToStreamOptions] = self.get.map(_.deadline(durationInMs))
    }

  /** The maximum event count EventStoreDB will return.
    */
  def withoutRequireLeader: SubscriptionOptions =
    new SubscriptionOptions {
      override def get: Try[SubscribeToStreamOptions] = self.get.map(_.notRequireLeader())
    }

  def fromEnd: SubscriptionOptions =
    new SubscriptionOptions {
      override def get: Try[SubscribeToStreamOptions] = self.get.map(_.fromEnd())
    }

  def fromStart: SubscriptionOptions =
    new SubscriptionOptions {
      override def get: Try[SubscribeToStreamOptions] = self.get.map(_.fromStart())
    }

}

object SubscriptionOptions {
  def default: SubscriptionOptions = new SubscriptionOptions {}
}
