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

    def decodeAs[B](
      implicit decoder: Decoder[B]
    ) = reader.getEventData.map { bytes =>
      decodeAccumulating[B](new String(bytes)) match {
        case Invalid(e) => ReadDecodeResult.Failure(e.toList)
        case Valid(a)   => ReadDecodeResult.Success(a)
      }
    }

  }

  implicit class ReadDecodeResultOps[A](val result: ReadDecodeResult[A]) {

    def map[B](f: A => B): ReadDecodeResult[B] =
      result match {
        case ReadDecodeResult.Success(value)  => ReadDecodeResult.Success(f(value))
        case ReadDecodeResult.Failure(errors) => ReadDecodeResult.Failure(errors)
      }

    def flatMap[B](f: A => ReadDecodeResult[B]): ReadDecodeResult[B] =
      result match {
        case ReadDecodeResult.Success(value)  => f(value)
        case ReadDecodeResult.Failure(errors) => ReadDecodeResult.Failure(errors)
      }

    def fold[B](failure: List[Error] => B, success: A => B): B =
      result match {
        case ReadDecodeResult.Success(value)  => success(value)
        case ReadDecodeResult.Failure(errors) => failure(errors)
      }

    def get: Option[A] =
      result match {
        case ReadDecodeResult.Success(value) => Some(value)
        case ReadDecodeResult.Failure(_)     => None
      }

    def getOrElse[B >: A](default: => B): B = get.getOrElse(default)

    def orElse[B >: A](that: => ReadDecodeResult[B]): ReadDecodeResult[B] =
      result match {
        case ReadDecodeResult.Success(value) => ReadDecodeResult.Success(value)
        case ReadDecodeResult.Failure(_)     => that
      }

  }
}
