// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.builder.client

import dolphin.builder.ClientBuilder
import dolphin.setting.EventStoreSettings

import cats.MonadThrow
import cats.effect.kernel.{MonadCancelThrow, Resource}
import com.eventstore.dbclient.*
import fs2.Stream

private[dolphin] object VolatileClientBuilder {

  def resource[F[_]: MonadThrow](
    options: EventStoreSettings
  ): Resource[F, EventStoreDBClient] = ClientBuilder.makeResource(EventStoreDBClient.create, options)

  def stream[F[_]: MonadCancelThrow](
    options: EventStoreSettings
  ): Stream[F, EventStoreDBClient] = ClientBuilder.makeStream(EventStoreDBClient.create, options)

}
