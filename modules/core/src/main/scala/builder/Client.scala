// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.client

import cats.MonadThrow
import cats.effect.kernel.{MonadCancelThrow, Resource}
import cats.syntax.applicative.*
import cats.syntax.applicativeError.*
import cats.syntax.flatMap.*
import com.eventstore.dbclient.*
import fs2.Stream

private[dolphin] object Client {

  def makeResource[F[_]: MonadThrow](
    host: String,
    port: Int,
    tls: Boolean = false,
  ): Resource[F, EventStoreDBClient] = Resource.eval {
    MonadThrow[F].attempt(
      EventStoreDBConnectionString
        .parse(s"esdb://$host:$port?tls=$tls")
        .pure[F]
    ) flatMap {
      case Left(e)  => e.raiseError[F, EventStoreDBClient]
      case Right(v) =>
        MonadThrow[F].attempt(EventStoreDBClient.create(v).pure[F]) flatMap {
          case Left(e)  => e.raiseError[F, EventStoreDBClient]
          case Right(v) => v.pure[F]
        }
    }
  }

  def makeStream[F[_]: MonadCancelThrow](
    host: String,
    port: Int,
    tls: Boolean = false,
  ): Stream[F, EventStoreDBClient] = Stream.resource(makeResource(host, port, tls))
}
