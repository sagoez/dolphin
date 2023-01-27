// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.internal.builder

import dolphin.*

import cats.MonadThrow
import cats.effect.kernel.{MonadCancelThrow, Resource}
import cats.syntax.applicativeError.*
import cats.syntax.flatMap.*
import com.eventstore.dbclient.{Endpoint, EventStoreDBClientSettings}
import fs2.Stream

private[internal] object ClientBuilder {

  def makeResource[F[_]: MonadThrow, A](
    fa: EventStoreDBClientSettings => A,
    options: Config
  ): Resource[F, A] = Resource.eval {
    MonadThrow[F].attempt(
      MonadThrow[F].pure(
        EventStoreDBClientSettings
          .builder()
          .tls(options.tls.tls)
          .addHost(new Endpoint(options.host.host, options.port.port))
          .defaultDeadline(options.deadline.getOrElse(Deadline))
          .discoveryInterval(options.discoveryInterval.getOrElse(DiscoveryInterval))
          .gossipTimeout(options.gossipTimeout.getOrElse(GossipTimeout))
          .keepAliveTimeout(options.keepAliveTimeout.getOrElse(KeepAliveTimeout))
          .keepAliveInterval(options.keepAliveInterval.getOrElse(KeepAliveInterval))
          .maxDiscoverAttempts(options.maxDiscoverAttempts.getOrElse(MaxDiscoverAttempts))
          .dnsDiscover(options.dnsDiscover)
          .throwOnAppendFailure(options.throwOnAppendFailure)
          .nodePreference(options.nodePreference.map(_.toJava).getOrElse(NodePreference.toJava))
          .tlsVerifyCert(options.tlsVerifyCert)
          .buildConnectionSettings()
      )
    ) flatMap {
      case Left(e)  => e.raiseError[F, A]
      case Right(v) =>
        MonadThrow[F].attempt(MonadThrow[F].pure(fa(v))) flatMap {
          case Left(e)  => e.raiseError[F, A]
          case Right(v) => MonadThrow[F].pure(v)
        }
    }
  }

  def makeStream[F[_]: MonadCancelThrow, A](
    fa: EventStoreDBClientSettings => A,
    options: Config
  ): Stream[F, A] = Stream.resource(makeResource[F, A](fa, options))

}
