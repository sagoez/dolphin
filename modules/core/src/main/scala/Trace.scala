// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin

import dolphin.internal.util.PrettyPrinter

import cats.Applicative
import cats.effect.kernel.Sync
import org.typelevel.log4cats.Logger
import sourcecode.{File, Line}

sealed trait Trace[F[_]] {

  def trace(
    message: String
  )(
    implicit line: Line,
    file: File
  ): F[Unit]

  def info(
    message: String
  )(
    implicit line: Line,
    file: File
  ): F[Unit]

  def error(
    cause: Throwable,
    message: Option[String]
  )(
    implicit line: Line,
    file: File
  ): F[Unit]

}

object Trace {

  def apply[F[_]](
    implicit F: Trace[F]
  ): Trace[F] = implicitly[Trace[F]]

  object NoOp {

    implicit def instance[F[_]: Applicative]: Trace[F] =
      new Trace[F] {

        override def trace(
          message: String
        )(
          implicit line: Line,
          file: File
        ): F[Unit] = Applicative[F].unit

        override def info(
          message: String
        )(
          implicit line: Line,
          file: File
        ): F[Unit] = Applicative[F].unit

        override def error(
          cause: Throwable,
          message: Option[String]
        )(
          implicit line: Line,
          file: File
        ): F[Unit] = Applicative[F].unit
      }
  }

  object StdOut {

    implicit def instance[F[_]: Sync]: Trace[F] =
      new Trace[F] {

        override def trace(
          message: String
        )(
          implicit line: Line,
          file: File
        ): F[Unit] = Sync[F].delay(Console.out.println(message))

        override def error(
          cause: Throwable,
          message: Option[String]
        )(
          implicit line: Line,
          file: File
        ): F[Unit] = Sync[F].delay(Console.err.println(PrettyPrinter.beautify(cause, message)(line, file)))

        override def info(
          message: String
        )(
          implicit line: Line,
          file: File
        ): F[Unit] = Sync[F].delay(Console.out.println(message))
      }
  }

  implicit def instance[F[_]: Logger]: Trace[F] =
    new Trace[F] {

      override def trace(
        message: String
      )(
        implicit line: Line,
        file: File
      ): F[Unit] = Logger[F].trace(message)

      override def error(
        cause: Throwable,
        message: Option[String]
      )(
        implicit line: Line,
        file: File
      ): F[Unit] = Logger[F].error(PrettyPrinter.beautify(cause, message))

      override def info(
        message: String
      )(
        implicit line: Line,
        file: File
      ): F[Unit] = Logger[F].info(message)
    }
}
