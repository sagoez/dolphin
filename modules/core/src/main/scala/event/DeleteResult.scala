// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.event

import java.util.concurrent.CompletableFuture

import cats.effect.kernel.Async
import cats.syntax.applicative.*
import cats.syntax.functor.*
import com.eventstore.dbclient.{DeleteResult as EventStoreDeleteResult, *}
import org.typelevel.log4cats.Logger

sealed abstract case class DeleteResult[F[_]: Async] private (
  private val completableFuture: CompletableFuture[EventStoreDeleteResult]
) { self =>

  protected def get: F[EventStoreDeleteResult] = Async[F].fromCompletableFuture[EventStoreDeleteResult](
    completableFuture.pure[F]
  )

  /** Returns the transaction log position of the stream deletion.
    */
  def getPosition: F[Position] = get.map(_.getPosition())

}

private[dolphin] object DeleteResult {

  def fromCompletableFuture[F[_]: Async](result: CompletableFuture[EventStoreDeleteResult]): DeleteResult[F] =
    new DeleteResult[F](result) {}

  def fromEventReadResult[F[_]: Async](result: EventStoreDeleteResult): DeleteResult[F] =
    new DeleteResult[F](CompletableFuture.completedFuture(result)) {}

  implicit class DeleteResultOps[F[_]: Async: Logger](val writeResult: CompletableFuture[EventStoreDeleteResult]) {

    import cats.syntax.applicativeError.*
    import cats.syntax.flatMap.*
    import cats.syntax.apply.*

    def toSafeAttempt: F[DeleteResult[F]] = Async[F].fromCompletableFuture(writeResult.pure[F]).attempt.flatMap {
      case Left(error)   => Logger[F].error(error)("Failed to delete from EventStore") *> Async[F].raiseError(error)
      case Right(result) => DeleteResult.fromEventReadResult(result).pure[F]
    }
  }

}
