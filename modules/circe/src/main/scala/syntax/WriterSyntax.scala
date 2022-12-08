// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.circe.syntax

import io.circe.Encoder

object writer extends WriterSyntax

trait WriterSyntax {

  implicit class WriterOps[F[_], A](val value: A) {

    /** Given an encoder, it will encode the value to a byte array.
      * @param encoder
      *   the encoder to use
      * @return
      */
    def toByteArray(implicit encoder: Encoder[A]): Array[Byte] = {
      val json  = encoder(value)
      val bytes = json.noSpaces.getBytes
      bytes
    }

  }
}
