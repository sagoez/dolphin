// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.outcome

import dolphin.concurrent.ExpectedRevision.*
import dolphin.concurrent.Position.*
import dolphin.concurrent.{ExpectedRevision, Position}

import cats.Applicative
import com.eventstore.dbclient

sealed trait Write[F[_]] {

  /** Transaction log position of the write. */
  def getLogPosition: F[Position]

  /** Next expected version of the stream. */
  def getNextExpectedRevision: F[ExpectedRevision]

}

object Write {

  private[dolphin] def make[F[_]: Applicative](
    ctx: dbclient.WriteResult
  ) =
    new Write[F] {

      def getNextExpectedRevision: F[ExpectedRevision] = Applicative[F].pure(ctx.getNextExpectedRevision.toScala)

      /** Transaction log position of the write. */
      def getLogPosition: F[Position] = Applicative[F].pure(ctx.getLogPosition.toScala)

    }
}
