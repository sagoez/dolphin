// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.concurrent

import dolphin.{CommitUnsigned, PrepareUnsigned}

final case class Position(commitUnsigned: CommitUnsigned, prepareUnsigned: PrepareUnsigned)

object Position {

  implicit final class PositionOps[A](private val self: Position) extends AnyVal {

    def toJava: com.eventstore.dbclient.Position =
      new com.eventstore.dbclient.Position(
        self.commitUnsigned,
        self.prepareUnsigned
      )
  }

  implicit final class PositionJavaOps[A](private val self: com.eventstore.dbclient.Position) extends AnyVal {

    def toScala: Position = Position(
      self.getCommitUnsigned,
      self.getPrepareUnsigned
    )
  }
}
