// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.suite

import org.scalacheck.Gen

object generator {
  def numericGen: Gen[Long] = Gen.chooseNum(Long.MinValue, Long.MaxValue)

}
