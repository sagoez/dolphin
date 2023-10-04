// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.concurrent

import dolphin.{CommitUnsigned, PrepareUnsigned}

final case class Position(commitUnsigned: CommitUnsigned, prepareUnsigned: PrepareUnsigned)

object Position {

  final implicit class PositionOps[A](private val self: Position) extends AnyVal {

    private[dolphin] def toJava: com.eventstore.dbclient.Position =
      new com.eventstore.dbclient.Position(
        self.commitUnsigned,
        self.prepareUnsigned
      )
  }

  final implicit class PositionJavaOps[A](private val self: com.eventstore.dbclient.Position) extends AnyVal {

    private[dolphin] def toScala: Position = Position(
      self.getCommitUnsigned,
      self.getPrepareUnsigned
    )
  }
}
