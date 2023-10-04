// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.outcome

import dolphin.concurrent.ExpectedRevision.*
import dolphin.concurrent.Position.*
import dolphin.concurrent.{ExpectedRevision, Position}

import com.eventstore.dbclient

sealed trait Write {

  /** Transaction log position of the write. */
  def getLogPosition: Position

  /** Next expected version of the stream. */
  def getNextExpectedRevision: ExpectedRevision

}

object Write {

  private[dolphin] def make(
    ctx: dbclient.WriteResult
  ): Write =
    new Write {

      def getNextExpectedRevision: ExpectedRevision = ctx.getNextExpectedRevision.toScala

      /** Transaction log position of the write. */
      def getLogPosition: Position = ctx.getLogPosition.toScala

    }
}
