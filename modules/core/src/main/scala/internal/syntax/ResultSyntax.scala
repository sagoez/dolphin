// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT
package dolphin.internal.syntax

import dolphin.trace.Trace

import cats.MonadThrow
import sourcecode.{File, Line}

private[dolphin] object result extends ResultSyntax

private[dolphin] trait ResultSyntax {

  private[dolphin] final implicit class PureOps[F[_], A](fa: F[A]) {
    import cats.syntax.applicativeError.*
    import cats.syntax.flatMap.*
    import cats.syntax.apply.*

    def withTraceAndTransformer[B](
      fb: A => B
    )(
      implicit file: File,
      line: Line,
      trace: Trace[F],
      A: MonadThrow[F]
    ): F[B] = fa.attempt.flatMap {
      case Left(exception) =>
        trace.error(exception, Some("Failed to obtain result from EventStore")) *> A.raiseError[B](exception)
      case Right(result)   => A.pure(fb(result))
    }

    def withTrace(
      implicit file: File,
      line: Line,
      trace: Trace[F],
      A: MonadThrow[F]
    ): F[Unit] = withTraceAndTransformer(_ => ())
  }
}
