// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.outcome

import dolphin.concurrent.Position
import dolphin.concurrent.Position.*

import cats.Applicative
import com.eventstore.dbclient

sealed trait Delete[F[_]] {

  /** Returns the transaction log position of the stream deletion. */
  def getPosition: F[Position]

}

object Delete {

  private[dolphin] def make[F[_]: Applicative](ctx: dbclient.DeleteResult): Delete[F] =
    new Delete[F] {

      /** Returns the transaction log position of the stream deletion. */
      def getPosition: F[Position] = Applicative[F].pure(ctx.getPosition.toScala)

    }

}
