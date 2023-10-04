// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.suite

import org.scalacheck.Gen

object generator {
  def posNumericGen: Gen[Long] = Gen.chooseNum(0L, Long.MaxValue)

}
