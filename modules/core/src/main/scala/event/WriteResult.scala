package dolphin.event

import java.util.concurrent.CompletableFuture

import cats.effect.kernel.Async
import cats.syntax.applicative.*
import cats.syntax.functor.*
import com.eventstore.dbclient.{ExpectedRevision, Position, WriteResult => EventStoreWriteResult}
import org.typelevel.log4cats.Logger

sealed abstract case class WriteResult[F[_]: Async] private (
  private val completableFuture: CompletableFuture[EventStoreWriteResult]
) { self =>

  def getLogPosition: F[Position] = Async[F].fromCompletableFuture(completableFuture.pure[F]).map(_.getLogPosition)

  def getNextExpectedRevision: F[ExpectedRevision] = Async[F]
    .fromCompletableFuture(completableFuture.pure[F])
    .map(_.getNextExpectedRevision)

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
