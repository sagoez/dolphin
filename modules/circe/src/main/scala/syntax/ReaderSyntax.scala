// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.circe.syntax

import dolphin.circe.domain.ReadDecodeResult
import dolphin.event.ReadResult

import cats.Functor
import cats.data.Validated.{Invalid, Valid}
import io.circe.parser.decodeAccumulating
import io.circe.{Decoder, Error}

object reader extends ReaderSyntax

trait ReaderSyntax {

  implicit class ReaderOps[F[_]: Functor](val reader: ReadResult[F]) {

    def decodeAs[B: Decoder] = reader.getEventData.map { bytes =>
      decodeAccumulating[B](new String(bytes)) match {
        case Invalid(e) => ReadDecodeResult.Failure(e.toList)
        case Valid(a)   => ReadDecodeResult.Success(a)
      }
    }

  }

  implicit class ReadDecodeResultOps[A](val result: ReadDecodeResult[A]) {

    /** Transforms the value of a successful result using the given function.
      *
      * @param f
      *   the function to apply
      * @return
      *   the result of the function
      */
    def map[B](f: A => B): ReadDecodeResult[B] =
      result match {
        case ReadDecodeResult.Success(value)  => ReadDecodeResult.Success(f(value))
        case ReadDecodeResult.Failure(errors) => ReadDecodeResult.Failure(errors)
      }

    /** Will perform the given function ```success``` if the result is a success, otherwise will perform the given
      * function ```failure```.
      *
      * @param failure
      *   the function to apply if the result is a failure
      * @param success
      *   the function to apply if the result is a success
      * @return
      */
    def fold[B](failure: List[Error] => B, success: A => B): B =
      result match {
        case ReadDecodeResult.Success(value)  => success(value)
        case ReadDecodeResult.Failure(errors) => failure(errors)
      }

    /** Returns the value if the result is a success, otherwise returns None.
      */
    def get: Option[A] =
      result match {
        case ReadDecodeResult.Success(value) => Some(value)
        case ReadDecodeResult.Failure(_)     => None
      }

    /** Returns the value if the result is a success, otherwise returns the given default value.
      * @param default
      *   the default value to return if the result is a failure
      */
    def getOrElse[B >: A](default: => B): B = get.getOrElse(default)

    /** If the result is a failure, it i'll return that instead
      * @param that
      *   the result to return if the result is a failure
      */
    def orElse[B >: A](that: => ReadDecodeResult[B]): ReadDecodeResult[B] =
      result match {
        case ReadDecodeResult.Success(value) => ReadDecodeResult.Success(value)
        case ReadDecodeResult.Failure(_)     => that
      }

  }
}
