// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.util.tests

import dolphin.util.PrettyPrinter

import weaver.FunSuite

// This test relies on actual line numbers, so it's not a very good test but it's better than nothing.
object PrettyPrinterSuite extends FunSuite {
  test("PrettyPrinter should return a string") {
    expect(PrettyPrinter.beautify(new Exception("test"), None).isInstanceOf[String])
  }

  test("PrettyPrinter should return a detailed message") {
    val dir = System.getProperty("user.dir")
    expect(
      PrettyPrinter
        .beautify(new Exception("Unexpected Exception"), Some("message"))
        .contains("Unexpected Exception")
    ) and expect(
      PrettyPrinter
        .beautify(new Exception("test"), Some("test"))
        .contains(dir + "/modules/tests/src/test/scala/util/PrettyPrinterSuite.scala:20")
    ) and expect(
      PrettyPrinter
        .beautify(new Exception("test"), Some("test"))
        .replaceAll("\n", " ")
        .substring(0, 241) == "  \uD83D\uDD25test\uD83D\uDD25 ❇️An unexpected error occurred: no hint available for this error: java.lang.Exception❇️  Stack trace: test ℹ️ maybe /Users/sjimenez/Desktop/Learning/event-store-db4s/modules/tests/src/test/scala/util/PrettyPrinterSuite.scala:24 ℹ️"
    )
  }
}
