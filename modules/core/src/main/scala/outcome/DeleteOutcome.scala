// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.outcome

import dolphin.concurrent.Position
import dolphin.concurrent.Position.*

import cats.Applicative
import com.eventstore.dbclient

sealed trait DeleteOutcome[F[_]] {

  /** Returns the transaction log position of the stream deletion. */
  def getPosition: F[Position]

}

object DeleteOutcome {

  private[dolphin] def make[F[_]: Applicative](result: dbclient.DeleteResult): DeleteOutcome[F] =
    new DeleteOutcome[F] {

      /** Returns the transaction log position of the stream deletion. */
      def getPosition: F[Position] = Applicative[F].pure(result.getPosition.toScala)

    }

}
