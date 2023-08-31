// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.concurrent

import dolphin.concurrent.ExpectedRevision.*
import dolphin.suite.generator.posNumericGen

import com.eventstore.dbclient
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object ExpectedRevisionSuite extends SimpleIOSuite with Checkers {

  test("Should be able to convert from ExpectedRevision to DbClient.ExpectedRevision") {
    // Technically, expected revision can't be negative, hence the use of posNumericGen
    forall(posNumericGen) { value =>
      expect(ExpectedRevision.Any.toJava == dbclient.ExpectedRevision.any()) and
        expect(ExpectedRevision.NoStream.toJava == dbclient.ExpectedRevision.noStream()) and
        expect(ExpectedRevision.StreamExists.toJava == dbclient.ExpectedRevision.streamExists()) and
        expect(ExpectedRevision.Exact(value).toJava == dbclient.ExpectedRevision.expectedRevision(value))
    }

  }

  test("Should be able to convert from DbClient.ExpectedRevision to ExpectedRevision") {
    forall(posNumericGen) { value =>
      expect(dbclient.ExpectedRevision.any().toScala == ExpectedRevision.Any) and
        expect(dbclient.ExpectedRevision.noStream().toScala == ExpectedRevision.NoStream) and
        expect(dbclient.ExpectedRevision.streamExists().toScala == ExpectedRevision.StreamExists) and
        expect(dbclient.ExpectedRevision.expectedRevision(value).toScala == ExpectedRevision.Exact(value))
    }
  }

  test("Should be able to convert from ExpectedRevision to DbClient.ExpectedRevision and back") {
    forall(posNumericGen) { value =>
      expect(ExpectedRevision.Any.toJava.toScala == ExpectedRevision.Any) and
        expect(ExpectedRevision.NoStream.toJava.toScala == ExpectedRevision.NoStream) and
        expect(ExpectedRevision.StreamExists.toJava.toScala == ExpectedRevision.StreamExists) and
        expect(ExpectedRevision.Exact(value).toJava.toScala == ExpectedRevision.Exact(value))
    }
  }

}
