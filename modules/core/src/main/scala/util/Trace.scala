// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.util

import org.typelevel.log4cats.Logger
import sourcecode.{File, Line}

sealed trait Trace[F[_]] {

  def trace(message: String): F[Unit]

  def error(cause: Throwable, message: Option[String]): F[Unit]

}

object Trace {
  def apply[F[_]](implicit F: Trace[F]): Trace[F] = implicitly[Trace[F]]

  def instance[F[_]: Logger](implicit line: Line, file: File): Trace[F] =
    new Trace[F] {
      override def trace(message: String): F[Unit] = Logger[F].error(message)

      override def error(
        cause: Throwable,
        message: Option[String]
      ): F[Unit] = Logger[F].error(PrettyPrinter.beautify(cause, message))

    }
}
