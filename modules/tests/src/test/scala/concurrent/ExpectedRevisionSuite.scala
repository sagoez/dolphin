// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.concurrent.tests

import dolphin.concurrent.ExpectedRevision
import dolphin.concurrent.ExpectedRevision.*
import dolphin.tests.generator.numericGen

import com.eventstore.dbclient
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object ExpectedRevisionSuite extends SimpleIOSuite with Checkers {

  test("Should be able to convert from ExpectedRevision to DbClient.ExpectedRevision") {
    forall(numericGen) { value =>
      expect(ExpectedRevision.Any.toJava == dbclient.ExpectedRevision.any())
      expect(ExpectedRevision.NoStream.toJava == dbclient.ExpectedRevision.noStream())
      expect(ExpectedRevision.StreamExists.toJava == dbclient.ExpectedRevision.streamExists())
      expect(ExpectedRevision.Exact(value).toJava == dbclient.ExpectedRevision.expectedRevision(value))
    }

  }

  test("Should be able to convert from DbClient.ExpectedRevision to ExpectedRevision") {
    forall(numericGen) { value =>
      expect(dbclient.ExpectedRevision.any().fromJava == ExpectedRevision.Any)
      expect(dbclient.ExpectedRevision.noStream().fromJava == ExpectedRevision.NoStream)
      expect(dbclient.ExpectedRevision.streamExists().fromJava == ExpectedRevision.StreamExists)
      expect(dbclient.ExpectedRevision.expectedRevision(value).fromJava == ExpectedRevision.Exact(value))
    }
  }

  test("Should be able to convert from ExpectedRevision to DbClient.ExpectedRevision and back") {
    forall(numericGen) { value =>
      expect(ExpectedRevision.Any.toJava.fromJava == ExpectedRevision.Any)
      expect(ExpectedRevision.NoStream.toJava.fromJava == ExpectedRevision.NoStream)
      expect(ExpectedRevision.StreamExists.toJava.fromJava == ExpectedRevision.StreamExists)
      expect(ExpectedRevision.Exact(value).toJava.fromJava == ExpectedRevision.Exact(value))
    }
  }

}
