// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.event

import java.util.concurrent.CompletableFuture

import dolphin.concurrent.ExpectedRevision
import dolphin.concurrent.ExpectedRevision.DbClientExpectedRevisionOps

import cats.effect.kernel.Async
import cats.syntax.applicative.*
import cats.syntax.functor.*
import com.eventstore.dbclient.WriteResult as EventStoreWriteResult
import org.typelevel.log4cats.Logger

/// TODO: Returning Position and ExpectedRevision is not very safe as it exposes the underlying implementation. We should wrap it in a case class.
sealed abstract case class WriteResult[F[_]: Async] private (
  private val completableFuture: CompletableFuture[EventStoreWriteResult]
) { self =>

  private def get: F[EventStoreWriteResult] = Async[F].fromCompletableFuture[EventStoreWriteResult](
    completableFuture.pure[F]
  )

  /** Returns the commit position.
    */
  def getCommitUnsigned: F[Long] = get.map(_.getLogPosition.getCommitUnsigned())

  /** Returns the prepare position.
    */
  def getPrepareUnsigned: F[Long] = get.map(_.getLogPosition.getPrepareUnsigned())

  /** Next expected version of the stream. Maps java to scala using reflection, in case of failure getting expected
    * revision of long type, it will throw an exception.
    */
  def getNextExpectedRevision: F[ExpectedRevision] = Async[F]
    .fromCompletableFuture(completableFuture.pure[F])
    .map(_.getNextExpectedRevision.fromJava)

}

private[dolphin] object WriteResult {

  def fromCompletableFuture[F[_]: Async](result: CompletableFuture[EventStoreWriteResult]): WriteResult[F] =
    new WriteResult[F](result) {}

  def fromEventWriteResult[F[_]: Async](result: EventStoreWriteResult): WriteResult[F] =
    new WriteResult[F](CompletableFuture.completedFuture(result)) {}

  implicit class WriteResultOps[F[_]: Async: Logger](val writeResult: CompletableFuture[EventStoreWriteResult]) {

    import cats.syntax.applicativeError.*
    import cats.syntax.flatMap.*
    import cats.syntax.apply.*

    def toSafeAttempt: F[WriteResult[F]] = Async[F].fromCompletableFuture(writeResult.pure[F]).attempt.flatMap {
      case Left(error)   => Logger[F].error(error)("Failed to read from EventStore") *> Async[F].raiseError(error)
      case Right(result) => WriteResult.fromEventWriteResult(result).pure[F]

    }
  }

}
