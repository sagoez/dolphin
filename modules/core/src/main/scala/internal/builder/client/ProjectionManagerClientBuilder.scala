// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.internal.builder.client

import dolphin.Config
import dolphin.internal.builder.ClientBuilder

import cats.MonadThrow
import cats.effect.kernel.{MonadCancelThrow, Resource}
import com.eventstore.dbclient.*
import fs2.Stream

object ProjectionManagerClientBuilder {

  def resource[F[_]: MonadThrow](
    options: Config
  ): Resource[F, EventStoreDBProjectionManagementClient] = ClientBuilder.makeResource(
    EventStoreDBProjectionManagementClient.create,
    options
  )

  def stream[F[_]: MonadCancelThrow](
    options: Config
  ): Stream[F, EventStoreDBProjectionManagementClient] = ClientBuilder.makeStream(
    EventStoreDBProjectionManagementClient.create,
    options
  )

}
