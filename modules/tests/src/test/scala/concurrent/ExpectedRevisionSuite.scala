// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.concurrent

import dolphin.concurrent.ExpectedRevision
import dolphin.concurrent.ExpectedRevision.*
import weaver.FunSuite
import com.eventstore.dbclient

object ExpectedRevisionSuite extends FunSuite {

  test("Should be able to convert from ExpectedRevision to DbClient.ExpectedRevision") {
    expect(ExpectedRevision.Any.toJava == dbclient.ExpectedRevision.any())
    expect(ExpectedRevision.NoStream.toJava == dbclient.ExpectedRevision.noStream())
    expect(ExpectedRevision.StreamExists.toJava == dbclient.ExpectedRevision.streamExists())
    expect(ExpectedRevision.Exact(1).toJava == dbclient.ExpectedRevision.expectedRevision(1))
  }

  test("Should be able to convert from DbClient.ExpectedRevision to ExpectedRevision") {
    expect(dbclient.ExpectedRevision.any().fromJava == ExpectedRevision.Any)
    expect(dbclient.ExpectedRevision.noStream().fromJava == ExpectedRevision.NoStream)
    expect(dbclient.ExpectedRevision.streamExists().fromJava == ExpectedRevision.StreamExists)
    expect(dbclient.ExpectedRevision.expectedRevision(1).fromJava == ExpectedRevision.Exact(1))
  }

  test("Should be able to convert from ExpectedRevision to DbClient.ExpectedRevision and back") {
    expect(ExpectedRevision.Any.toJava.fromJava == ExpectedRevision.Any)
    expect(ExpectedRevision.NoStream.toJava.fromJava == ExpectedRevision.NoStream)
    expect(ExpectedRevision.StreamExists.toJava.fromJava == ExpectedRevision.StreamExists)
    expect(ExpectedRevision.Exact(1).toJava.fromJava == ExpectedRevision.Exact(1))
  }

}
