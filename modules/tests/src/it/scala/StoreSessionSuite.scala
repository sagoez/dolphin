package dolphin.tests

import cats.effect.IO
import cats.effect.kernel.Resource
import dolphin.StoreSession
import dolphin.concurrent.ExpectedRevision
import dolphin.option.ReadOptions
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.noop.NoOpLogger
import weaver.IOSuite
import weaver.scalacheck.{CheckConfig, Checkers}
import fs2.Stream

// TODO: Write more tests...
object StoreSessionSuite extends IOSuite with Checkers {
  override def checkConfig: CheckConfig              = CheckConfig.default.copy(minimumSuccessful = 1)
  implicit val logger: SelfAwareStructuredLogger[IO] = NoOpLogger[IO]

  override type Res = StoreSession[IO]

  override def sharedResource: Resource[IO, Res] = StoreSession.resource("localhost", 2113, tls = false)

  test("Should be able to create a session and write a dummy event to event store database") { session =>
    forall(generator.nonEmptyStringGen) { streamName =>
      for {
        writeResult          <- session.write(streamName, (s"test-event-".getBytes, None), "test-data")
        nextExpectedRevision <- writeResult.getNextExpectedRevision
        commitUnsigned       <- writeResult.getCommitUnsigned
        prepareUnsigned      <- writeResult.getPrepareUnsigned
      } yield expect(nextExpectedRevision == ExpectedRevision.Exact(0)) and expect(commitUnsigned > 0L) and expect(
        prepareUnsigned > 0L
      )
    }
  }

  test("Should be able to create a session and read a dummy event from event store database") { session =>
    forall(generator.nonEmptyStringGen) { streamName =>
      (for {
        _               <- Stream.eval(session.write(streamName, (s"test-event-".getBytes, None), "test-data"))
        readResult      <- Stream.eval(session.read(streamName, ReadOptions.default))
        readResultEvent <- readResult.getEventData.map(new String(_))
      } yield expect(readResultEvent == "test-event-")).compile.lastOrError
    }
  }

}
