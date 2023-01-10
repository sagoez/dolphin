package dolphin.tests

import dolphin.PersistentSession
import dolphin.setting.EventStoreSettings

import cats.effect.IO
import cats.effect.kernel.Resource
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.noop.NoOpLogger
import weaver.IOSuite
import weaver.scalacheck.{CheckConfig, Checkers}

object PersistentSessionSuite extends IOSuite with Checkers {
  override def checkConfig: CheckConfig              = CheckConfig.default.copy(minimumSuccessful = 1)
  implicit val logger: SelfAwareStructuredLogger[IO] = NoOpLogger[IO]

  override type Res = PersistentSession[IO]

  override def sharedResource: Resource[IO, Res] = PersistentSession.resource(EventStoreSettings.Default)

  // TODO: Add tests
}
