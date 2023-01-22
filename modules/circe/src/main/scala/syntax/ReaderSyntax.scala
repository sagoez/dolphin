// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.circe.syntax

import dolphin.circe.domain.ReadDecodeResult
import dolphin.outcome.ReadOutcome

import cats.data.Validated.{Invalid, Valid}
import fs2.Stream
import io.circe.Decoder
import io.circe.parser.decodeAccumulating

object reader extends ReaderSyntax

trait ReaderSyntax {

  implicit class ReaderOps[F[_]](val reader: ReadOutcome[F]) {

    /** Given a decoder, it will try to decode to a ReadDecodeResult.
      *
      * @return
      *   A Stream[F, ReadDecodeResult[A]]. The stream will emit a ReadDecodeResult for each event.
      */
    def as[B: Decoder]: Stream[F, ReadDecodeResult[B]] = reader.getEventData.map { bytes =>
      decodeAccumulating[B](new String(bytes)) match {
        case Invalid(e) => ReadDecodeResult.Failure(e.toList)
        case Valid(a)   => ReadDecodeResult.Success(a)
      }
    }

  }
}
