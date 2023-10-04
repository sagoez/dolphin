// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.outcome

import dolphin.concurrent.Position
import dolphin.concurrent.Position.*

import com.eventstore.dbclient

sealed trait Delete {

  /** Returns the transaction log position of the stream deletion. */
  def getPosition: Position

}

object Delete {

  private[dolphin] def make(ctx: dbclient.DeleteResult): Delete =
    new Delete {

      /** Returns the transaction log position of the stream deletion. */
      def getPosition: Position = ctx.getPosition.toScala

    }

}
