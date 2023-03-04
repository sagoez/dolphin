// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.suite

import org.scalacheck.Gen

object generator {

  val nonEmptyStringGen: Gen[String] = Gen
    .chooseNum(21, 40)
    .flatMap { n =>
      Gen.buildableOfN[String, Char](n, Gen.alphaChar)
    }
}
