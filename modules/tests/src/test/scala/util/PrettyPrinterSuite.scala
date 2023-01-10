// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.util.tests

import dolphin.util.PrettyPrinter

import weaver.FunSuite

object PrettyPrinterSuite extends FunSuite {
  test("PrettyPrinter should return a string") {
    expect(PrettyPrinter.beautify(new Exception("test"), None).isInstanceOf[String])
  }

  test("PrettyPrinter should return a detailed message") {
    expect(
      PrettyPrinter
        .beautify(new Exception("Unexpected Exception"), Some("message"))
        .contains("Unexpected Exception")
    ) and expect(
      PrettyPrinter
        .beautify(new Exception("test"), Some("test"))
        .replaceAll("\n", " ")
        .substring(0, 241)
        .contains(
          " unexpected error occurred: no hint available for this error: java.lang.Exception❇️  Stack trace: test ℹ️ maybe "
        )
    )
  }
}
