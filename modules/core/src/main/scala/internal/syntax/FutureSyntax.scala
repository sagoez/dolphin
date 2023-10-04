// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.internal.syntax

import scala.concurrent.{ExecutionContext, Future}

import cats.effect.IO
import cats.effect.unsafe.IORuntime

private[dolphin] object future extends FutureSyntax

private[dolphin] sealed trait IOFuture[F[_]] {

  def convert[A](
    fa: => F[A]
  )(
    implicit runtime: IORuntime
  ): Future[A]

  def convertUnit[A](
    fa: => F[A]
  )(
    implicit ec: ExecutionContext,
    runtime: IORuntime
  ): Unit = convert(fa).onComplete(_ => ())
}

private[dolphin] object IOFuture {
  def apply[F[_]: IOFuture]: IOFuture[F] = implicitly[IOFuture[F]]

  implicit val ioFuture: IOFuture[IO] =
    new IOFuture[IO] {

      override def convert[A](
        fa: => IO[A]
      )(
        implicit runtime: IORuntime
      ): Future[A] = fa.unsafeToFuture()
    }

}

private[dolphin] trait FutureSyntax {

  implicit class FutureSyntaxOps[F[_]: IOFuture, A](val fa: F[A]) {

    def toFuture(
      implicit runtime: IORuntime
    ): Future[A] = IOFuture[F].convert(fa)

    def toUnit(
      implicit ec: ExecutionContext,
      runtime: IORuntime
    ): Unit = IOFuture[F].convert(fa).onComplete(_ => ())
  }

}
