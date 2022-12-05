// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.circe.domain

import io.circe.Error

sealed trait ReadDecodeResult[+A] extends Product with Serializable

object ReadDecodeResult {
  case class Success[A](value: A)         extends ReadDecodeResult[A]
  case class Failure(errors: List[Error]) extends ReadDecodeResult[Nothing]
}
